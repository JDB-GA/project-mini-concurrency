const money = new Intl.NumberFormat("en-US", {style: "currency", currency: "USD"});
const percent = new Intl.NumberFormat("en-US", {style: "percent", maximumFractionDigits: 2});

const form = document.getElementById("rates");
const head = document.getElementById("table-head");
const body = document.getElementById("table-body");
const message = document.getElementById("message");
const summary = document.getElementById("summary");
const buttons = document.querySelectorAll(".actions button");

const employeeColumns = [
    {title: "ID", value: e => e.id},
    {title: "Name", value: e => e.name},
    {title: "Role", value: e => formatRole(e.role)},
    {title: "Joined", value: e => e.joinedDate},
    {title: "Completion", value: e => percent.format(e.completion), number: true},
    {title: "Salary", value: e => money.format(e.salary), number: true}
];

const changeColumns = [
    {title: "ID", value: c => c.id},
    {title: "Name", value: c => c.name},
    {title: "Role", value: c => formatRole(c.role)},
    {title: "Years", value: c => c.yearsWorked, number: true},
    {title: "Completion", value: c => percent.format(c.completion), number: true},
    {title: "Before", value: c => money.format(c.oldSalary), number: true},
    {title: "After", value: c => money.format(c.newSalary), number: true, className: c => c.rate > 0 ? "up" : ""},
    {title: "Raise", value: c => percent.format(c.rate), number: true},
    {title: "Increase", value: c => money.format(c.newSalary - c.oldSalary), number: true},
    {title: "Thread", value: c => c.thread, className: () => "thread"}
];

function formatRole(role) {
    return role.charAt(0) + role.slice(1).toLowerCase();
}

function query() {
    const params = new URLSearchParams();
    new FormData(form).forEach((value, key) => {
        if (value !== "") {
            params.append(key, value);
        }
    });
    return params.toString();
}

async function request(url, options) {
    const response = await fetch(url, options);
    const data = await response.json();
    if (!response.ok) {
        throw new Error(data.message || response.statusText);
    }
    return data;
}

function render(columns, rows, rowClass) {
    const headRow = document.createElement("tr");
    columns.forEach(column => {
        const th = document.createElement("th");
        th.textContent = column.title;
        if (column.number) {
            th.className = "number";
        }
        headRow.appendChild(th);
    });
    head.replaceChildren(headRow);

    body.replaceChildren(...rows.map(row => {
        const tr = document.createElement("tr");
        tr.className = rowClass ? rowClass(row) : "";
        columns.forEach(column => {
            const td = document.createElement("td");
            td.textContent = column.value(row);
            td.className = [column.number ? "number" : "", column.className ? column.className(row) : ""].join(" ").trim();
            tr.appendChild(td);
        });
        return tr;
    }));
}

function showSummary(report) {
    document.getElementById("processed").textContent = report.processed;
    document.getElementById("raised").textContent = report.raised;
    document.getElementById("skipped").textContent = report.skipped;
    document.getElementById("total").textContent = money.format(report.totalIncrease);
    document.getElementById("duration").textContent = report.durationMillis + " ms";
    summary.hidden = false;
}

function showReport(report) {
    showSummary(report);
    render(changeColumns, report.changes, change => change.rate > 0 ? "" : "no-raise");
}

function setMessage(text, isError) {
    message.textContent = text;
    message.classList.toggle("error", Boolean(isError));
}

async function run(action) {
    buttons.forEach(button => button.disabled = true);
    setMessage("Working...");
    try {
        await action();
    } catch (error) {
        setMessage(error.message, true);
    } finally {
        buttons.forEach(button => button.disabled = false);
    }
}

async function loadEmployees() {
    const employees = await request("/api/employees");
    summary.hidden = true;
    render(employeeColumns, employees);
    setMessage(employees.length + " employees loaded from CSV.");
}

async function preview() {
    const report = await request("/api/salaries/preview?" + query());
    showReport(report);
    setMessage("Preview only. The CSV file was not changed.");
}

async function apply() {
    const result = await request("/api/salaries/apply?" + query(), {method: "POST"});
    showReport(result.report);
    setMessage("Saved updated salaries to " + result.outputFile);
}

document.getElementById("preview").addEventListener("click", () => run(preview));
document.getElementById("apply").addEventListener("click", () => run(apply));
document.getElementById("reset").addEventListener("click", () => {
    form.reset();
    run(loadEmployees);
});

run(loadEmployees);
