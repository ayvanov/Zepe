import { Hono } from "https://deno.land/x/hono@v3.1.2/mod.ts";
import {
  html,
  serveStatic,
} from "https://deno.land/x/hono@v3.1.2/middleware.ts";

import {
  dirname,
  fromFileUrl,
} from "https://deno.land/std@0.178.0/path/mod.ts";
import { Context } from "https://deno.land/x/hono@v3.1.2/context.ts";

const __filename = fromFileUrl(import.meta.url);
const __dirname = dirname(fromFileUrl(import.meta.url));

type DateValuePair = {
  value: number;
  date: Date | undefined;
};

class Format {
  static round(num: number) {
    return Math.round(num);
  }

  static money(num: number) {
    return new Intl.NumberFormat("ru-RU", {
      style: "currency",
      currency: "RUB",
      notation: "compact",
    }).format(num);
  }
}

class MonthMeta {
  static salaryMultiplier = 1;
  static advanceDays = 15;
  static advancePayDay = 25;
  static restPayDay = 10;
  #monthSlice: string;
  #nextMonthSlice: string;
  #advanceSlice: string;
  salary = 0;
  monthNum;
  year;

  get total() {
    return this.#monthSlice.length;
  }
  get workdays() {
    return Array.from(this.#monthSlice).filter((n) => n === "0").length;
  }
  get holydays() {
    return Array.from(this.#monthSlice).filter((n) => n === "1").length;
  }
  get salaryPerDay() {
    if (this.workdays === 0) return 0;
    return Format.round(
      (this.salary / this.workdays) * MonthMeta.salaryMultiplier
    );
  }
  get advanceWorkdays() {
    return Array.from(this.#advanceSlice).filter((n) => n === "0").length;
  }
  get advanceValue() {
    return Format.round(this.salaryPerDay * this.advanceWorkdays);
  }
  get advanceDate() {
    return new Date(
      this.year,
      this.monthNum - 1,
      this.#monthSlice.slice(0, MonthMeta.advancePayDay).lastIndexOf("0") + 1
    );
  }
  get restValue() {
    return this.salary - this.advanceValue;
  }
  get restDate() {
    if (!this.#nextMonthSlice) return undefined;
    return new Date(
      this.year,
      this.monthNum,
      this.#nextMonthSlice.slice(0, MonthMeta.restPayDay).lastIndexOf("0") + 1
    );
  }

  constructor(
    monthSlice: string,
    nextMonthSlice: string,
    monthNum: number,
    salary: number = 0,
    year: number
  ) {
    this.#monthSlice = monthSlice;
    this.#nextMonthSlice = nextMonthSlice;
    this.#advanceSlice = monthSlice.slice(0, MonthMeta.advanceDays);
    this.salary = salary;
    this.monthNum = monthNum;
    this.year = year || new Date().getFullYear();
  }

  toJSON() {
    return {
      monthNum: this.monthNum,
      salary: this.salary,
      totalDays: this.total,
      workdays: this.workdays,
      holydays: this.holydays,
      salaryPerDay: this.salaryPerDay,
      advanceWorkdays: this.advanceWorkdays,
      advanceValue: this.advanceValue,
      advanceDate: this.advanceDate,
      restValue: this.restValue,
      restDate: this.restDate,
      slice: this.#monthSlice,
      advanceSlice: this.#advanceSlice,
    };
  }
}

class ZepeCalc {
  static async getYearData({ year = 0, salary = 0 }) {
    year = year || new Date().getFullYear();
    const yearDataSlices = await this.fetchYearSlices(year);
    const monthsMeta: MonthMeta[] = [];
    for (const monthNum of Object.keys(yearDataSlices)) {
      monthsMeta.push(
        new MonthMeta(
          yearDataSlices[Number(monthNum)],
          yearDataSlices[Number(monthNum) + 1],
          Number(monthNum) + 1,
          salary,
          year
        )
      );
    }
    return { [year]: monthsMeta };
  }

  static async fetchYearSlices(year: number) {
    let cachedYearData = { value: null };
    let kv;
    try {
      kv = await Deno.openKv();
      cachedYearData = await kv.get(["yearData", year]);
    } catch (e) {
      console.error("Deno KV is not available", e);
    }
    let yearData = "";
    if (!cachedYearData.value) {
      const url = `https://isdayoff.ru/api/getdata?year=${year}`;
      const res = await fetch(url);
      yearData = await res.text();
      console.log("fetched from remote");
      try {
        await kv?.set(["yearData", year], yearData);
        console.log("Cached to KV");
      } catch (e) {
        console.error("Deno KV is not available", e);
      }
    } else {
      yearData = String(cachedYearData.value);
      console.log("fetched from cache");
    }
    const nextYearFirstMonthUrl = `https://isdayoff.ru/api/getdata?year=${
      year + 1
    }&month=1`;
    // always prefetch next january
    const nextRes = await fetch(nextYearFirstMonthUrl);
    const nextYearFirstMonthData = await nextRes.text();
    const dataSlices = [];
    if (yearData.length) {
      let sliceOffset = 0;
      for (let month = 1; month <= 12; month++) {
        const daysInMonth = new Date(year, month, 0).getDate();
        dataSlices.push(yearData.slice(sliceOffset, sliceOffset + daysInMonth));
        sliceOffset += daysInMonth;
      }
      if (nextYearFirstMonthData.length) {
        dataSlices.push(nextYearFirstMonthData);
      }
    } else {
      throw new Error("Failed to fetch Year Data");
    }
    return dataSlices;
  }
}

const app = new Hono();

app.use("/icons/android/*", serveStatic({ root: "./" }));
app.use("/icons/ios/*", serveStatic({ root: "./" }));
app.use("/icons/windows11/*", serveStatic({ root: "./" }));
app.use("/public/*", serveStatic({ root: "./" }));
app.use(
  "/favicon.ico",
  serveStatic({ path: "./icons/android/android-launchericon-48-48.png" })
);
app.use("/manifest.json", serveStatic({ path: "./manifest.json" }));
app.use("/sw.js", serveStatic({ path: "./sw.js" }));

const settingsButtonHtml =
  '<a id="settings-btn" href="#" onclick="openModal();">&nbsp;</a>';

app.get("/", async (c: Context) => {
  const decoder = new TextDecoder("utf-8");
  const htmlSource = await Deno.readFile("index.html");
  return c.html(
    decoder.decode(htmlSource).replace("{app}", settingsButtonHtml)
  );
});

app.get("/api/:s/:y?", async (c: Context) =>
  c.json(
    await ZepeCalc.getYearData({
      salary: Number(c.req.param("s")),
      year: Number(c.req.param("y")),
    })
  )
);

app.get("/:salary/:year?", async (c: Context) => {
  const year = Number(c.req.param("year") || new Date().getFullYear());
  const data = await ZepeCalc.getYearData({
    salary: Number(c.req.param("salary")),
    year,
  });
  const now = new Date();
  const MonthMetaFragment = (props: MonthMeta) => {
    const monthName = new Date(
      now.getFullYear(),
      props.monthNum - 1,
      1
    ).toLocaleDateString("ru-Ru", { month: "long" });
    return html`
      <div class="month">
        <h3>${monthName}</h3>
        ${DateValueBlock({
          date: props.advanceDate,
          value: props.advanceValue,
        })}
        ${DateValueBlock({ date: props.restDate, value: props.restValue })}
      </div>
    `;
  };

  const DateValueBlock = (props: DateValuePair) => {
    const textDate = props.date?.toLocaleDateString("ru-Ru", {
      day: "numeric",
      month: "long",
      weekday: "short",
    });
    const pastClass = props.date && new Date() > props.date ? " past" : "";
    return html`
      <div class="date-value">
        <div class="value${pastClass}">${Format.money(props.value)}</div>
        <div class="separator">&mdash;</div>
        <div class="date">${textDate || ""}</div>
      </div>
    `;
  };
  let htmlFragment = `<h1 class="header"><i></i><b class="year">${year}</b>${settingsButtonHtml}</h1>`;
  for (const year in data) {
    if (Object.prototype.hasOwnProperty.call(data, year)) {
      const yearData = data[year];
      for (const monthMeta of yearData) {
        if (monthMeta.monthNum > 12) {
          continue;
        }
        htmlFragment += MonthMetaFragment(monthMeta);
      }
    }
  }
  const decoder = new TextDecoder("utf-8");
  const htmlSource = await Deno.readFile("index.html");

  return c.html(decoder.decode(htmlSource).replace("{app}", htmlFragment));
});

Deno.serve(app.fetch);
