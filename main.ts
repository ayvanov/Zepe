import { serve } from "https://deno.land/std@0.179.0/http/server.ts";
import { Hono } from "https://deno.land/x/hono@v3.0.2/mod.ts";
import { html, raw } from "https://deno.land/x/hono@v3.0.2/middleware.ts";

type ValueDate = {
  value: number;
  date: Date;
};
type CalcResponce = {
  target: Date;
  monthNum: number;
  salary: number;
  year: number;
  dataSlices: string[];
  monthSlice: string;
  firstHalf: string;
  days: {
    total: number;
    workdays: number;
    holydays: number;
    salaryPerDay: number;
    advanceWorkdays: number;
  };
  advance: ValueDate;
  rest: ValueDate;
  next: ValueDate | undefined;
};

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
  static async getData(
    salary = 0,
    offset = 0,
    getNext = true,
  ): Promise<CalcResponce> {
    salary = Number(salary);
    const now = new Date();
    const target = new Date(
      now.getFullYear(),
      now.getMonth() - 1 - (offset * -1),
      1,
    );
    const year = target.getFullYear();
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
    const { advance: next } = getNext
      ? await this.getData(salary, offset + 1, false)
      : { advance: undefined };
    return {
      target,
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
        ),
      },
      rest: {
        value: this.round(salary - advance),
        date: new Date(
          target.getFullYear(),
          target.getMonth() + 1,
          nextMonthSlice.slice(0, this.restPayDay).lastIndexOf("0") + 1,
        ),
      },
      next,
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

app.get("/:s/:o?", async (c) => {
  const data = await ZepeCalc.getData(
    Number(c.req.param("s") || 0),
    Number(c.req.param("o") || 0),
  );
  const title = `Выплаты за ${
    data.target.toLocaleDateString("ru-RU", { month: "long" })
  } при зарплате ${ZepeCalc.formatMoney(data.salary)}`;

  const DateValueBlock = (props: ValueDate) =>
    html`
    <div class="date-value">
      <div class="value">${ZepeCalc.formatMoney(props.value)}</div>
      <div class="separator">&mdash;</div>
      <div class="date">${props.date.toLocaleDateString("ru-Ru", { day: "numeric", month: "long" })
    }</div>
    </div>
  `;

  return c.html(
    html`
    <!DOCTYPE html>
      <head>
      <title>${title}</title>
      <meta charset="UTF-8">
      <meta name="viewport" content="width=device-width, initial-scale=1.0">
      <style>
          @import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;900&display=swap');
          body {
            font-family: 'Inter', sans-serif;
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
      <body style="text-align:center; padding:20px;">
        <h2>${title}</h2>
        <div class="values-container">
          ${DateValueBlock(data.advance)}
          ${DateValueBlock(data.rest)}
          ${data.next && DateValueBlock(data.next)}
          </div>
      </body>
      `,
  );
});

serve(app.fetch);
