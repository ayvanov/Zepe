import { serve } from "https://deno.land/std@0.179.0/http/server.ts";
import { Hono } from "https://deno.land/x/hono@v3.0.2/mod.ts";
import { html, raw } from "https://deno.land/x/hono@v3.0.2/middleware.ts";

type ValueDate = {
  value: number;
  date: Date;
};
type CalcResponce = {
  target:Date;
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
  const title = `Выплаты за ${data.target.toLocaleDateString("ru-RU",{month:"long"})} при зарплате ${ZepeCalc.formatMoney(data.salary)}`;
  const dateIcon = `<span class="icon"><svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor">
    <path strokeLinecap="round" strokeLinejoin="round" d="M6.75 3v2.25M17.25 3v2.25M3 18.75V7.5a2.25 2.25 0 012.25-2.25h13.5A2.25 2.25 0 0121 7.5v11.25m-18 0A2.25 2.25 0 005.25 21h13.5A2.25 2.25 0 0021 18.75m-18 0v-7.5A2.25 2.25 0 015.25 9h13.5A2.25 2.25 0 0121 11.25v7.5m-9-6h.008v.008H12v-.008zM12 15h.008v.008H12V15zm0 2.25h.008v.008H12v-.008zM9.75 15h.008v.008H9.75V15zm0 2.25h.008v.008H9.75v-.008zM7.5 15h.008v.008H7.5V15zm0 2.25h.008v.008H7.5v-.008zm6.75-4.5h.008v.008h-.008v-.008zm0 2.25h.008v.008h-.008V15zm0 2.25h.008v.008h-.008v-.008zm2.25-4.5h.008v.008H16.5v-.008zm0 2.25h.008v.008H16.5V15z" />
  </svg></span>`;
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
          .date {
            font-weight: 500;
          }
          .value {
            font-size: 1.5rem;
            font-weight: 800;
          }
          .values-container{
            display: flex;
            justify-content: center;
            align-items: center;
            flex-direction: column;
          }
          .date-value {
            margin:30px;
            text-align:center;
          }
          svg {
            display:inline-block;
            vertical-align:text-bottom;
            width:1.5rem;
            height:1.5rem;
          }
          
      </style>
      </head>
      <body style="text-align:center; padding:20px;">
        <h2>${title}</h2>
        <div class="values-container">
          <div class="date-value">
            <div class="value">${ZepeCalc.formatMoney(data.advance.value)}</div>
            <div class="date">${data.advance.date.toLocaleDateString("ru-Ru")}</div>
          </div>
          <div class="date-value">
            <div class="value">${ZepeCalc.formatMoney(data.rest.value)}</div>
            <div class="date">${data.rest.date.toLocaleDateString("ru-Ru")}</div> 
          </div>
          <div class="date-value">
            <div class="value">${ZepeCalc.formatMoney(data.next?.value || 0)}</div>
            <div class="date">${data.next?.date.toLocaleDateString("ru-Ru")}</div> 
          </div>
        </div>
      </body>
      `,
  );
});

serve(app.fetch);
