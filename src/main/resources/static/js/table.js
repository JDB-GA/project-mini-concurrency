function headerRow(columns) {
    const row = document.createElement("tr");
    columns.forEach(column => {
        const cell = document.createElement("th");
        cell.textContent = column.title;
        cell.className = column.number ? "number" : "";
        row.appendChild(cell);
    });
    return row;
}

function bodyRow(columns, item, rowClass) {
    const row = document.createElement("tr");
    row.className = rowClass ? rowClass(item) : "";
    columns.forEach(column => {
        const cell = document.createElement("td");
        cell.textContent = column.value(item);
        cell.className = [column.number ? "number" : "", column.className ? column.className(item) : ""].join(" ").trim();
        row.appendChild(cell);
    });
    return row;
}

export function renderTable(head, body, columns, items, rowClass) {
    head.replaceChildren(headerRow(columns));
    body.replaceChildren(...items.map(item => bodyRow(columns, item, rowClass)));
}
