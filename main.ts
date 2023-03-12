import { serve } from "https://deno.land/std@0.179.0/http/server.ts";
import { Hono } from "https://deno.land/x/hono@v3.0.2/mod.ts";
import { html } from "https://deno.land/x/hono@v3.0.2/middleware.ts"

class ZepeCalc {
  static advanceSlice = 15;
  static salaryMultiplier = 1;
  static advancePayDay = 25;
  static restPayDay = 10;

  static round(num: number) {
    return Math.round(num + Number.EPSILON);
  }
  static formatMoney(num: number) {
    return new Intl.NumberFormat("ru-RU", {
      style: "currency",
      currency: "RUB",
      notation: "compact",
    }).format(num);
  }
  static async getData(salary = 0, offset = 0) {
    salary = Number(salary);
    const now = new Date();
    const target = new Date(
      now.getFullYear(),
      now.getMonth() - 1 - (offset * -1),
      1,
    );
    const year = target.getFullYear();
    const monthName = target.toLocaleDateString("ru-RU", { month: "long" });
    const monthNum = target.getMonth() + 1;
    const url = `https://isdayoff.ru/api/getdata?year=${year}`;
    const api = await fetch(url);
    const yearData = await api.text();
    const dataSlices = [];
    let sliceOffset = 0;
    for (let month = 1; month <= 12; month++) {
      const daysInMonth = new Date(target.getFullYear(), month, 0).getDate();
      dataSlices.push(yearData.slice(sliceOffset, sliceOffset + daysInMonth));
      sliceOffset += daysInMonth;
    }
    const monthSlice = dataSlices[monthNum - 1];
    const nextMonthSlice = dataSlices[monthNum];
    const firstHalf = monthSlice.slice(0, this.advanceSlice);
    const advanceWorkdays =
      Array.from(firstHalf).filter((n) => n === "0").length;
    const total = monthSlice.length;
    const workdays = Array.from(monthSlice).filter((n) => n === "0").length;
    const holydays = Array.from(monthSlice).filter((n) => n === "1").length;
    const salaryPerDay = this.round(salary / workdays * this.salaryMultiplier);
    const advance = this.round(advanceWorkdays * salaryPerDay);

    return {
      title: `Выплаты за ${monthName} ${year} при зарплате ${this.formatMoney(salary)} (на руки)`,
      monthNum,
      salary,
      year,
      dataSlices,
      monthSlice,
      firstHalf,
      days: {
        total,
        workdays,
        holydays,
        salaryPerDay,
        advanceWorkdays,
      },
      advance: {
        value: advance,
        date: new Date(
          target.getFullYear(),
          target.getMonth(),
          monthSlice.slice(0, this.advancePayDay).lastIndexOf("0") + 1,
        ).toLocaleDateString("ru-RU"),
      },
      rest: {
        value: this.round(salary - advance),
        date: new Date(
          target.getFullYear(),
          target.getMonth() + 1,
          nextMonthSlice.slice(0, this.restPayDay).lastIndexOf("0") + 1,
        ).toLocaleDateString("ru-RU"),
      },
    };
  }
}

const app = new Hono();

app.get(
  "/api/:salary/:offset?",
  async (c) =>
    c.json(
      await ZepeCalc.getData(
        Number(c.req.param("salary") || 0),
        Number(c.req.param("offset") || 0),
      ),
    ),
);

app.get("/", (c) => {
  return c.html(
    html`<!DOCTYPE html>
      <head>
      <style>
          @import url('https://fonts.googleapis.com/css2?family=Inter&display=swap');
          body {
            font-family: 'Inter', sans-serif;
          }
      </style>
      </head>
      <body>
        <h4>Hello from Zepe!</h4>
      </body>
      `
  )
});

serve(app.fetch);
