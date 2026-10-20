async function request(url, options) {
    const response = await fetch(url, options);
    const data = await response.json();
    if (!response.ok) {
        throw new Error(data.message || response.statusText);
    }
    return data;
}

export function rateQuery(form) {
    const params = new URLSearchParams();
    new FormData(form).forEach((value, key) => {
        if (value !== "") {
            params.append(key, value);
        }
    });
    return params.toString();
}

export function fetchEmployees() {
    return request("/api/employees");
}

export function previewRaises(query) {
    return request("/api/salaries/preview?" + query);
}

export function applyRaises(query) {
    return request("/api/salaries/apply?" + query, {method: "POST"});
}

export function fetchStatus() {
    return request("/api/status");
}

export function resetData() {
    return request("/api/salaries/reset", {method: "POST"});
}
