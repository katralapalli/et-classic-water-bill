// ══════════════════════════════════════════════════════
// CONFIG — change if Spring Boot runs on a different port
// ══════════════════════════════════════════════════════
const BASE_URL = 'http://localhost:8080';

// ══ State ══
let allReadings = [];
let allFlats    = [];

// ══════════════════════════════════════════════════════
// INIT
// ══════════════════════════════════════════════════════
document.addEventListener('DOMContentLoaded', () => {
    const now = new Date();
    document.getElementById('headerDate').textContent =
        now.toLocaleDateString('en-IN', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' });
    document.getElementById('monthBadge').textContent =
        now.toLocaleDateString('en-IN', { month: 'long', year: 'numeric' }) + ' — Readings';
    loadReadings();
    loadFlats();
});

// ══════════════════════════════════════════════════════
// TABS
// ══════════════════════════════════════════════════════
function switchTab(tab) {
    document.querySelectorAll('.tab-btn').forEach((b, i) =>
        b.classList.toggle('active', (i === 0 && tab === 'readings') || (i === 1 && tab === 'flats'))
    );
    document.querySelectorAll('.tab-panel').forEach(p => p.classList.remove('active'));
    document.getElementById('tab-' + tab).classList.add('active');
}

// ══════════════════════════════════════════════════════
// READINGS — LOAD
// ══════════════════════════════════════════════════════
async function loadReadings() {
    try {
        const res = await fetch(`${BASE_URL}/api/readings/current-month`);
        if (!res.ok) throw new Error('API error ' + res.status);
        allReadings = await res.json();
        renderReadings();
        updateStats();
    } catch (e) {
        document.getElementById('readingsBody').innerHTML =
            `<tr><td colspan="8">
                <div class="loading" style="color:var(--danger)">
                    ⚠️ Could not load readings. Is Spring Boot running?<br>
                    <small>${e.message}</small>
                </div>
             </td></tr>`;
    }
}

// ══════════════════════════════════════════════════════
// READINGS — RENDER
// ══════════════════════════════════════════════════════
function renderReadings() {
    const tbody = document.getElementById('readingsBody');
    tbody.innerHTML = '';

    allReadings.forEach(item => {
        const flat         = allFlats.find(f => f.flatNumber === item.flatNumber);
        const displayName  = flat ? (flat.tenantName  || flat.ownerName)  : item.flatNumber;
        const displayPhone = flat ? (flat.tenantPhone || flat.ownerPhone) : '—';
        const isFresh      = item.id === null || item.id === undefined;
        const readingDate  = isFresh
            ? new Date().toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' })
            : new Date(item.readingDate).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });

        const tr = document.createElement('tr');
        tr.dataset.id         = item.id ?? 'null';
        tr.dataset.flatNumber = item.flatNumber;

        tr.innerHTML = `
            <td><strong style="color:var(--primary)">${item.flatNumber}</strong></td>
            <td>${displayName}</td>
            <td style="color:var(--text-muted);font-size:0.82rem">${displayPhone}</td>
            <td style="color:var(--text-muted);font-size:0.82rem">
                ${isFresh ? `<span class="badge badge-fresh">New</span>` : readingDate}
            </td>
            <td class="reading-cell"
                data-id="${item.id ?? 'null'}"
                data-flat="${item.flatNumber}"
                data-name="${displayName}"
                data-phone="${displayPhone}">
                <div class="reading-display">
                    <span class="reading-value">${item.reading ?? 0}</span>
                    <span class="edit-hint">✏️ edit</span>
                </div>
                <div class="reading-input-group">
                    <input type="number" class="reading-input" value="${item.reading ?? 0}" min="0"/>
                    <button class="icon-btn icon-btn-save save-btn" title="Save">✔</button>
                    <button class="icon-btn icon-btn-cancel cancel-btn" title="Cancel">✖</button>
                    <div class="spinner"></div>
                </div>
            </td>
            <td class="consumption-cell">
                <span class="${item.consumption ? 'badge badge-value' : 'badge badge-zero'}">${item.consumption ?? 0}</span>
            </td>
            <td class="charges-cell">
                <strong style="color:${item.charges ? 'var(--success)' : 'var(--text-muted)'}">
                    ${item.charges ? '₹' + item.charges.toFixed(2) : '₹0.00'}
                </strong>
            </td>
            <td>
                <button class="btn btn-outline btn-sm"
                        onclick="openPastReadings('${item.flatNumber}', '${displayName}')">
                    📋 Past
                </button>
            </td>
        `;
        tbody.appendChild(tr);
    });

    attachReadingEvents();
}

// ══════════════════════════════════════════════════════
// STATS
// ══════════════════════════════════════════════════════
function updateStats() {
    const total   = allReadings.length;
    const entered = allReadings.filter(r => r.id !== null && r.id !== undefined).length;
    const pending = total - entered;
    const charges = allReadings.reduce((s, r) => s + (r.charges || 0), 0);

    document.getElementById('statTotal').textContent   = total;
    document.getElementById('statEntered').textContent = entered;
    document.getElementById('statPending').textContent = pending;
    document.getElementById('statCharges').textContent = '₹' + charges.toFixed(0);
}

// ══════════════════════════════════════════════════════
// READING — INLINE EDIT
// ══════════════════════════════════════════════════════
function attachReadingEvents() {
    document.querySelectorAll('.reading-cell').forEach(cell => {
        cell.querySelector('.reading-display').addEventListener('click', () => enterEdit(cell));
        cell.querySelector('.save-btn').addEventListener('click',   () => saveReading(cell.closest('tr')));
        cell.querySelector('.cancel-btn').addEventListener('click', () => exitEdit(cell));
        cell.querySelector('.reading-input').addEventListener('keydown', e => {
            if (e.key === 'Enter')  saveReading(cell.closest('tr'));
            if (e.key === 'Escape') exitEdit(cell);
        });
    });
}

function enterEdit(cell) {
    cell.querySelector('.reading-display').style.display = 'none';
    cell.querySelector('.reading-input-group').style.display = 'flex';
    cell.querySelector('.reading-input').focus();
}

function exitEdit(cell) {
    const orig = cell.querySelector('.reading-value').textContent.trim();
    cell.querySelector('.reading-input').value = orig;
    cell.querySelector('.reading-display').style.display = 'flex';
    cell.querySelector('.reading-input-group').style.display = 'none';
}

async function saveReading(row) {
    const cell    = row.querySelector('.reading-cell');
    const id      = cell.dataset.id;
    const input   = cell.querySelector('.reading-input');
    const newVal  = input.value.trim();
    const spinner = cell.querySelector('.spinner');
    const saveBtn = cell.querySelector('.save-btn');

    if (newVal === '' || isNaN(newVal)) {
        showToast('Enter a valid reading value.', 'danger');
        return;
    }

    spinner.style.display = 'inline-block';
    saveBtn.disabled = true;

    try {
        const isNew  = !id || id === 'null';
        const url    = `${BASE_URL}/api/readings`;
        const method = isNew ? 'POST' : 'PUT';
        const body   = isNew
            ? JSON.stringify({ flatNumber: cell.dataset.flat, reading: parseFloat(newVal) })
            : JSON.stringify({ id: parseInt(id), reading: parseFloat(newVal) });

        const res = await fetch(url, {
            method,
            headers: { 'Content-Type': 'application/json' },
            body
        });
        if (!res.ok) throw new Error('Server error ' + res.status);
        const result = await res.json();

        // Update UI
        cell.querySelector('.reading-value').textContent = newVal;
        row.querySelector('.consumption-cell').innerHTML =
            `<span class="badge badge-value">${result.consumption}</span>`;
        row.querySelector('.charges-cell').innerHTML =
            `<strong style="color:var(--success)">₹${parseFloat(result.charges).toFixed(2)}</strong>`;

        // Stamp new id
        if (result.id) {
            cell.dataset.id = result.id;
            row.dataset.id  = result.id;
        }

        // Update local state
        const idx = allReadings.findIndex(r => r.flatNumber === cell.dataset.flat);
        if (idx !== -1) {
            allReadings[idx].reading     = parseFloat(newVal);
            allReadings[idx].consumption = result.consumption;
            allReadings[idx].charges     = result.charges;
            allReadings[idx].id          = result.id || allReadings[idx].id;
        }

        exitEdit(cell);
        updateStats();
        showToast(`✅ Reading saved for ${cell.dataset.flat}`, 'success');

    } catch (e) {
        showToast('❌ Failed to save: ' + e.message, 'danger');
    } finally {
        spinner.style.display = 'none';
        saveBtn.disabled = false;
    }
}

// ══════════════════════════════════════════════════════
// PAST READINGS MODAL
// ══════════════════════════════════════════════════════
async function openPastReadings(flatNumber, name) {
    document.getElementById('pastModalTitle').textContent = `📋 Past Readings — ${flatNumber} (${name})`;
    document.getElementById('pastModalContent').innerHTML =
        '<div class="loading"><div class="loading-spinner"></div>Loading…</div>';
    document.getElementById('pastModal').classList.add('open');

    try {
        const res = await fetch(`${BASE_URL}/api/readings/flat/${flatNumber}`);
        if (!res.ok) throw new Error('API error');
        const data = await res.json();

        if (!data.length) {
            document.getElementById('pastModalContent').innerHTML =
                '<div class="no-data">No past readings found for this flat.</div>';
            return;
        }

        const rows = data.map(r => `
            <tr>
                <td>${new Date(r.readingDate).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' })}</td>
                <td><strong style="color:var(--primary)">${r.reading ?? 0}</strong></td>
                <td><span class="badge badge-value">${r.consumption ?? 0}</span></td>
                <td><strong style="color:var(--success)">₹${r.charges ? parseFloat(r.charges).toFixed(2) : '0.00'}</strong></td>
                <td style="color:var(--text-muted);font-size:0.78rem">
                    ${r.updatedAt ? new Date(r.updatedAt).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' }) : '—'}
                </td>
            </tr>
        `).join('');

        document.getElementById('pastModalContent').innerHTML = `
            <table class="past-table">
                <thead>
                    <tr>
                        <th>Reading Date</th>
                        <th>Reading</th>
                        <th>Consumption</th>
                        <th>Charges</th>
                        <th>Updated</th>
                    </tr>
                </thead>
                <tbody>${rows}</tbody>
            </table>
        `;
    } catch (e) {
        document.getElementById('pastModalContent').innerHTML =
            `<div class="no-data" style="color:var(--danger)">Error loading data.</div>`;
    }
}

function closePastModal() {
    document.getElementById('pastModal').classList.remove('open');
}

document.getElementById('pastModal').addEventListener('click', e => {
    if (e.target === document.getElementById('pastModal')) closePastModal();
});

// ══════════════════════════════════════════════════════
// GENERATE BILL (text file)
// ══════════════════════════════════════════════════════
function generateBill() {
    if (!allReadings.length) { showToast('No readings loaded.', 'danger'); return; }

    const dueDate = getDueDate();
    const lines   = [];

    allReadings.forEach(item => {
        const flat        = allFlats.find(f => f.flatNumber === item.flatNumber);
        const name        = flat ? (flat.tenantName || flat.ownerName) : item.flatNumber;
        const currReading = item.reading     || 0;
        const consumption = item.consumption || 0;
        const prevReading = currReading - consumption;
        const charges     = item.charges     || 0;
        const base        = Math.min(consumption, 20000) * 0.015;
        const extra       = Math.max(0, charges - base);

        lines.push(`Name & Flat # : ${name} ${item.flatNumber}`);
        lines.push(`Reading(Curr,Prev,Difference): ${currReading}, ${prevReading}, ${consumption} Liters`);
        lines.push(`Amount Payable: Rs.${charges.toFixed(2)}/- (${base.toFixed(2)} + ${extra.toFixed(2)})`);
        lines.push(`Note: Please pay your amount before ${dueDate} to avoid penalty.`);
        lines.push('');
        lines.push('─'.repeat(60));
        lines.push('');
    });

    downloadFile(lines.join('\n'), `water-bill-${getCurrentMonthStr()}.txt`, 'text/plain');
    showToast('📄 Bill generated!', 'success');
}

// ══════════════════════════════════════════════════════
// GENERATE CSV
// ══════════════════════════════════════════════════════
function generateCSV() {
    if (!allReadings.length) { showToast('No readings loaded.', 'danger'); return; }

    const header = ['flat_number', 'name', 'phone', 'pres_reading', 'prev_reading', 'consumption', 'charges'];
    const rows   = allReadings.map(item => {
        const flat        = allFlats.find(f => f.flatNumber === item.flatNumber);
        const name        = flat ? (flat.tenantName  || flat.ownerName)  : '';
        const phone       = flat ? (flat.tenantPhone || flat.ownerPhone) : '';
        const curr        = item.reading     || 0;
        const consumption = item.consumption || 0;
        const prev        = curr - consumption;
        const charges     = item.charges     || 0;
        return [item.flatNumber, `"${name}"`, phone, curr, prev, consumption, charges.toFixed(2)].join(',');
    });

    downloadFile([header.join(','), ...rows].join('\n'), `water-bill-${getCurrentMonthStr()}.csv`, 'text/csv');
    showToast('📥 CSV generated!', 'success');
}

// ══════════════════════════════════════════════════════
// FLATS — LOAD & RENDER
// ══════════════════════════════════════════════════════
async function loadFlats() {
    try {
        const res = await fetch(`${BASE_URL}/api/flats`);
        if (!res.ok) throw new Error('API error ' + res.status);
        allFlats = await res.json();
        renderFlats(allFlats);
        if (allReadings.length) renderReadings();
    } catch (e) {
        document.getElementById('flatsGrid').innerHTML =
            `<div class="loading" style="color:var(--danger)">⚠️ Could not load flats.</div>`;
    }
}

function renderFlats(flats) {
    document.getElementById('flatCount').textContent = `${flats.length} flat${flats.length !== 1 ? 's' : ''}`;
    const grid = document.getElementById('flatsGrid');
    grid.innerHTML = '';

    flats.forEach(flat => {
        const card = document.createElement('div');
        card.className = 'flat-card';

        card.innerHTML = `
            <div class="flat-card-header">
                <span class="flat-number">${flat.flatNumber}</span>
                <span style="font-size:0.75rem;opacity:0.8">ID: ${flat.id}</span>
            </div>
            <div class="flat-card-body">
                <div class="flat-info-label">Owner</div>
                <div class="flat-info-row">👤 <strong>${flat.ownerName}</strong></div>
                <div class="flat-info-row">📞 ${flat.ownerPhone || '—'}</div>

                <div class="flat-info-label" style="margin-top:0.75rem">Tenant</div>
                <div class="tenant-edit-row">
                    <input class="tenant-input" id="tname-${flat.flatNumber}"
                           placeholder="Tenant name" value="${flat.tenantName || ''}"/>
                </div>
                <div class="tenant-edit-row">
                    <input class="tenant-input" id="tphone-${flat.flatNumber}"
                           placeholder="Tenant phone" value="${flat.tenantPhone || ''}"/>
                    <button class="btn btn-success btn-sm" onclick="saveTenant('${flat.flatNumber}')">Save</button>
                </div>
            </div>
        `;
        grid.appendChild(card);
    });
}

function filterFlats() {
    const q = document.getElementById('flatSearch').value.toLowerCase();
    const filtered = allFlats.filter(f =>
        f.flatNumber.toLowerCase().includes(q) ||
        f.ownerName.toLowerCase().includes(q)  ||
        (f.tenantName && f.tenantName.toLowerCase().includes(q))
    );
    renderFlats(filtered);
}

async function saveTenant(flatNumber) {
    const name  = document.getElementById(`tname-${flatNumber}`).value.trim();
    const phone = document.getElementById(`tphone-${flatNumber}`).value.trim();

    try {
        const res = await fetch(`${BASE_URL}/api/flats/${flatNumber}/tenant`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ tenantName: name || null, tenantPhone: phone || null })
        });
        if (!res.ok) throw new Error('Server error ' + res.status);
        const updated = await res.json();

        const idx = allFlats.findIndex(f => f.flatNumber === flatNumber);
        if (idx !== -1) {
            allFlats[idx].tenantName  = updated.tenantName;
            allFlats[idx].tenantPhone = updated.tenantPhone;
        }

        showToast(`✅ Tenant updated for ${flatNumber}`, 'success');
        renderReadings();

    } catch (e) {
        showToast('❌ Failed to update tenant: ' + e.message, 'danger');
    }
}

// ══════════════════════════════════════════════════════
// HELPERS
// ══════════════════════════════════════════════════════
function showToast(msg, type = 'info') {
    const c = document.getElementById('toastContainer');
    const t = document.createElement('div');
    t.className   = `toast toast-${type}`;
    t.textContent = msg;
    c.appendChild(t);
    setTimeout(() => t.remove(), 3500);
}

function downloadFile(content, filename, mime) {
    const a  = document.createElement('a');
    a.href   = URL.createObjectURL(new Blob([content], { type: mime }));
    a.download = filename;
    a.click();
}

function getCurrentMonthStr() {
    const now = new Date();
    return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`;
}

function getDueDate() {
    const now = new Date();
    const due = new Date(now.getFullYear(), now.getMonth() + 1, 8);
    return due.toLocaleDateString('en-IN', { day: '2-digit', month: '2-digit', year: 'numeric' }).replace(/\//g, '-');
}
