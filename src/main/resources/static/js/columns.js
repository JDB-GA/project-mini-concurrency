export const money = new Intl.NumberFormat("en-US", {style: "currency", currency: "USD"});
const percent = new Intl.NumberFormat("en-US", {style: "percent", maximumFractionDigits: 2});

function formatRole(role) {
    return role.charAt(0) + role.slice(1).toLowerCase();
}

export const employeeColumns = [
    {title: "ID", value: e => e.id},
    {title: "Name", value: e => e.name},
    {title: "Role", value: e => formatRole(e.role)},
    {title: "Joined", value: e => e.joinedDate},
    {title: "Completion", value: e => percent.format(e.completion), number: true},
    {title: "Salary", value: e => money.format(e.salary), number: true}
];

export const changeColumns = [
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
