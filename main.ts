import { serve } from "https://deno.land/std@0.179.0/http/server.ts";
import { Hono } from "https://deno.land/x/hono@v3.0.2/mod.ts";

class ZepeCalc {
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
      now.getMonth() - (offset * -1),
      1,
    );
    const year = target.getFullYear();
    const monthName = target.toLocaleDateString("ru-RU", { month: "long" });
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
    const monthSlice = dataSlices[target.getMonth()];
    const nextMonthSlice = dataSlices[target.getMonth() + 1];
    const firstHalf = monthSlice.slice(0, 15);
    const advanceWorkdays =
      Array.from(firstHalf).filter((n) => n === "0").length;
    const total = monthSlice.length;
    const workdays = Array.from(monthSlice).filter((n) => n === "0").length;
    const holydays = Array.from(monthSlice).filter((n) => n === "1").length;
    const salaryPerDay = this.round(salary / workdays * 1);
    const advance = this.round(advanceWorkdays * salaryPerDay);

    return {
      title: `Выплаты за ${monthName} ${year} при зарплате ${
        this.formatMoney(salary)
      } (на руки)`,
      month: target.getMonth(),
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
          monthSlice.slice(0, 25).lastIndexOf("0") + 1,
        ).toLocaleDateString("ru-RU"),
      },
      rest: {
        value: this.round(salary - advance),
        date: new Date(
          target.getFullYear(),
          target.getMonth() + 1,
          nextMonthSlice.slice(0, 10).lastIndexOf("0") + 1,
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
serve(app.fetch);
