import {applyRaises, fetchEmployees, fetchStatus, previewRaises, rateQuery, resetData} from "./api.js";
import {changeColumns, employeeColumns, money} from "./columns.js";
import {renderTable} from "./table.js";

const form = document.getElementById("rates");
const head = document.getElementById("table-head");
const body = document.getElementById("table-body");
const message = document.getElementById("message");
const summary = document.getElementById("summary");
const buttons = document.querySelectorAll(".actions button");
const dataStatus = document.getElementById("data-status");

function setMessage(text, isError) {
    message.textContent = text;
    message.classList.toggle("error", Boolean(isError));
}

function showStatus(status) {
    dataStatus.textContent = status.original ? "Original data" : "Updated data";
    dataStatus.title = status.activeFile;
    dataStatus.classList.toggle("updated", !status.original);
}

function showReport(report) {
    document.getElementById("processed").textContent = report.processed;
    document.getElementById("raised").textContent = report.raised;
    document.getElementById("skipped").textContent = report.skipped;
    document.getElementById("total").textContent = money.format(report.totalIncrease);
    document.getElementById("duration").textContent = report.durationMillis + " ms";
    summary.hidden = false;
    renderTable(head, body, changeColumns, report.changes, change => change.rate > 0 ? "" : "no-raise");
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
    const [employees, status] = await Promise.all([fetchEmployees(), fetchStatus()]);
    summary.hidden = true;
    showStatus(status);
    renderTable(head, body, employeeColumns, employees);
    setMessage(employees.length + " employees loaded from " + (status.original ? "the original file." : "the updated file."));
}

async function preview() {
    showReport(await previewRaises(rateQuery(form)));
    setMessage("Preview only. Nothing was saved.");
}

async function apply() {
    const result = await applyRaises(rateQuery(form));
    showReport(result.report);
    showStatus(await fetchStatus());
    setMessage("Saved to " + result.outputFile + ". Apply again to stack raises, or reset to start over.");
}

document.getElementById("preview").addEventListener("click", () => run(preview));
document.getElementById("apply").addEventListener("click", () => run(apply));
document.getElementById("reset").addEventListener("click", () => run(async () => {
    await resetData();
    form.reset();
    await loadEmployees();
}));

run(loadEmployees);
