import { serve } from "https://deno.land/std@0.179.0/http/server.ts";
import { Hono } from "https://deno.land/x/hono@v3.0.2/mod.ts";
import { html } from "https://deno.land/x/hono@v3.0.2/middleware.ts";

type DateValuePair = {
  value: number;
  date: Date;
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
    return new Date(
      this.year,
      this.monthNum,
      this.#monthSlice.slice(0, MonthMeta.restPayDay).lastIndexOf("0") + 1,
    );
  }

  constructor(
    monthSlice: string,
    monthNum: number,
    salary: number = 0,
    year: number,
  ) {
    this.#monthSlice = monthSlice;
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
    };
  }
}

type CalcResponce = {
  target: Date;
  monthNum: number;
  salary: number;
  year: number;
  dataSlices: string[];
  monthSlice: string;
  firstHalf: string;
  days: MonthMeta;
  advance: DateValuePair;
  rest: DateValuePair;
  next: DateValuePair | undefined;
};

class ZepeCalc {
  static async getYearData({ year = 0, salary = 0 }) {
    year = year || new Date().getFullYear();
    const yearDataSlices = await this.fetchYearSlices(year);
    const monthsMeta: MonthMeta[] = [];
    for (const monthNum of Object.keys(yearDataSlices)) {
      monthsMeta.push(
        new MonthMeta(
          yearDataSlices[Number(monthNum)],
          Number(monthNum) + 1,
          salary,
          year,
        ),
      );
    }
    return { [year]: monthsMeta };
  }

  static async fetchYearSlices(year: number) {
    let yearData = localStorage.getItem(year.toString());
    if (!yearData) {
      const url = `https://isdayoff.ru/api/getdata?year=${year}`;
      const api = await fetch(url);
      yearData = await api.text();
      localStorage.setItem(year.toString(), yearData);
    }
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
  console.log(data);
  const DateValueBlock = (props: DateValuePair) => {
    const textDate = props.date.toLocaleDateString("ru-Ru", {
      day: "numeric",
      month: "long",
    });
    return html`
    <div class="date-value">
      <div class="value">${Format.money(props.value)}</div>
      <div class="separator">&mdash;</div>
      <div class="date">${textDate}</div>
    </div>
  `;
  };

  return c.html(
    html`
    <!DOCTYPE html>
      <head>
      <title></title>
      <meta charset="UTF-8">
      <meta name="viewport" content="width=device-width, initial-scale=1.0">
      <style>
          @import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;900&display=swap');
          body {
            font-family: 'Inter', sans-serif;
            text-align:center; 
            padding:20px;
          }
          .values-container{
            display: flex;
            align-items: center;
            flex-direction: column;
          }
          .date-value {
            margin:20px;
            display:flex;
            flex-direction:row;
            min-width:280px;
            place-content: space-between;
            place-items: center;
          }
          .date {
            font-weight: 500;
            font-size: 1rem;
          }
          .separator{
            font-weight: 500;
            font-size: 2rem;
          }
          .value {
            font-size: 1.5rem;
            font-weight: 800;
          }
          
      </style>
      </head>
      <body>
        <div class="values-container">
          
          </div>
      </body>
      `,
  );
});

serve(app.fetch);
