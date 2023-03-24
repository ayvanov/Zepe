import { serve } from "https://deno.land/std@0.179.0/http/server.ts";
import { Hono } from "https://deno.land/x/hono@v3.1.2/mod.ts";
import {
  html,
  raw,
  serveStatic,
} from "https://deno.land/x/hono@v3.1.2/middleware.ts";

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
      this.salary / this.workdays * MonthMeta.salaryMultiplier,
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
      this.#monthSlice.slice(0, MonthMeta.advancePayDay).lastIndexOf("0") + 1,
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
      this.#nextMonthSlice.slice(0, MonthMeta.restPayDay).lastIndexOf("0") + 1,
    );
  }

  constructor(
    monthSlice: string,
    nextMonthSlice: string,
    monthNum: number,
    salary: number = 0,
    year: number,
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
          year,
        ),
      );
    }
    return { [year]: monthsMeta };
  }

  static async fetchYearSlices(year: number) {
    const url = `https://isdayoff.ru/api/getdata?year=${year}`;
    const api = await fetch(url);
    const yearData = await api.text();
    const dataSlices = [];
    let sliceOffset = 0;
    for (let month = 1; month <= 12; month++) {
      const daysInMonth = new Date(year, month, 0).getDate();
      dataSlices.push(yearData.slice(sliceOffset, sliceOffset + daysInMonth));
      sliceOffset += daysInMonth;
    }
    return dataSlices;
  }
}

const app = new Hono();

app.use('/icons/*', serveStatic({ root: './icons' }))
app.use("/manifest.json", serveStatic({ path: "./manifest.json" }));
app.use("/sw.js", serveStatic({ path: "./sw.js" }));

app.get("/api/:s/:y?", async (c) =>
  c.json(
    await ZepeCalc.getYearData({
      salary: Number(c.req.param("s")),
      year: Number(c.req.param("y")),
    }),
  ));

app.get("/:salary/:year?", async (c) => {
  const data = await ZepeCalc.getYearData({
    salary: Number(c.req.param("salary")),
    year: Number(c.req.param("year")),
  });
  const now = new Date();
  const MonthMetaFragment = (props: MonthMeta) => {
    const monthName = new Date(now.getFullYear(), props.monthNum - 1, 1)
      .toLocaleDateString("ru-Ru", { month: "long" });
    return html`
    <div class="month">
      <h3>${monthName}</h3>
      ${DateValueBlock({ date: props.advanceDate, value: props.advanceValue })}
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
    const pastClass = (props.date && new Date() > props.date) ? " past" : "";
    return html`
    <div class="date-value">
      <div class="value${pastClass}">${Format.money(props.value)}</div>
      <div class="separator">&mdash;</div>
      <div class="date">${textDate || ""}</div>
    </div>
  `;
  };
  let htmlFragment = "";
  for (const year in data) {
    if (Object.prototype.hasOwnProperty.call(data, year)) {
      const yearData = data[year];
      for (const monthMeta of yearData) {
        htmlFragment += MonthMetaFragment(monthMeta);
      }
    }
  }
  return c.html(
    html`
    <!DOCTYPE html>
      <head>
      <title>Zepe</title>
      <meta charset="UTF-8">
      <link rel="icon" href="/icons/android-launchericon-48-48.png" type="image/png" />
      <meta name="viewport" content="width=device-width, initial-scale=1.0">
      <link rel="manifest" href="/manifest.json" />
      <style>
          @import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;800&display=swap');
          body {
            font-family: 'Inter', sans-serif;
            text-align:center; 
            background: #f1f5f9;
          }
          .month {
            box-shadow: 0 1px 3px 0 rgb(0 0 0 / 0.1), 0 1px 2px -1px rgb(0 0 0 / 0.1);
            transition: box-shadow 0.3s ease-in-out;
            padding:0.5rem 1rem;
            margin:.5rem auto;
            max-width:35rem;
            background: #fff;
          }
          .month:hover {
            box-shadow: 0 10px 15px -3px rgb(0 0 0 / 0.1), 0 4px 6px -4px rgb(0 0 0 / 0.1);
          }
          .month > h3 {
            text-transform:capitalize;
            margin-top: .2rem;
          }
          .date-value {
            margin:.5rem .5rem 1rem .5rem;
            display: grid;
            grid-template-columns: repeat(3, minmax(0, 1fr));
            place-items: center;
          }
          .date {
            font-weight: 500;
            font-size: .9rem;
            place-self: start;
            align-self: center;
          }
          .separator{
            font-weight: 400;
            font-size: 1.5rem;
            margin: 0 .5rem 0 .5rem;
          }
          .value {
            font-size: 1.5rem;
            font-weight: 800;
            place-self: start;
            align-self: center;
          }
          .value.past {
            opacity:.5;
          }
      </style>
      <script>
        if (typeof navigator.serviceWorker !== 'undefined') {
          navigator.serviceWorker.register('/sw.js').then(
            (registration) => {
              console.log("Service worker registration succeeded:", registration);
            },
          )
        }
      </script>
      </head>
      <body>
        ${raw(htmlFragment)}
      </body>
      `,
  );
});

serve(app.fetch);
