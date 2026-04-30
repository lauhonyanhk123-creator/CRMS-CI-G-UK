import { useEffect, useState } from 'react';
import axios from 'axios';

const API = '/api/v1';
const PO_STATUSES = ['DRAFT', 'PENDING', 'APPROVED', 'ORDERED', 'PARTIAL', 'RECEIVED', 'CANCELLED'];
const PR_STATUSES = ['DRAFT', 'SUBMITTED', 'APPROVED', 'REJECTED', 'CONVERTED'];
const PO_COLORS = { DRAFT: 'bg-gray-100 text-gray-600', PENDING: 'bg-amber-100 text-amber-700', APPROVED: 'bg-green-100 text-green-700', ORDERED: 'bg-blue-100 text-blue-700', PARTIAL: 'bg-orange-100 text-orange-700', RECEIVED: 'bg-teal-100 text-teal-700', CANCELLED: 'bg-red-100 text-red-500' };
const PR_COLORS = { DRAFT: 'bg-gray-100 text-gray-600', SUBMITTED: 'bg-blue-100 text-blue-700', APPROVED: 'bg-green-100 text-green-700', REJECTED: 'bg-red-100 text-red-600', CONVERTED: 'bg-purple-100 text-purple-700' };

function Modal({ title, onClose, children, size = 'max-w-2xl' }) {
  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
      <div className={`bg-white rounded-xl w-full ${size} max-h-[90vh] overflow-y-auto shadow-2xl`}>
        <div className="flex items-center justify-between px-6 py-4 border-b border-gray-200 sticky top-0 bg-white rounded-t-xl">
          <h3 className="text-lg font-semibold text-slate-800">{title}</h3>
          <button onClick={onClose} className="text-slate-400 hover:text-slate-600 text-2xl leading-none w-8 h-8 flex items-center justify-center rounded-lg hover:bg-gray-100">&times;</button>
        </div>
        <div className="p-6">{children}</div>
      </div>
    </div>
  );
}

function Field({ label, children }) { return <div><label className="block text-sm font-medium text-slate-700 mb-1">{label}</label>{children}</div>; }
function Input({ className = '', ...props }) { return <input {...props} className={`w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 ${className}`} />; }
function Select({ children, ...props }) { return <select {...props} className={`w-full px-3 py-2 border border-gray-300 rounded-lg text-sm bg-white focus:outline-none focus:ring-2 focus:ring-blue-500 ${props.className || ''}`}>{children}</select>; }
function Textarea({ className = '', ...props }) { return <textarea {...props} className={`w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 ${className}`} />; }
function Badge({ children, color = 'gray' }) { const colors = { green: 'bg-green-100 text-green-700', red: 'bg-red-100 text-red-700', amber: 'bg-amber-100 text-amber-700', gray: 'bg-gray-100 text-gray-600', blue: 'bg-blue-100 text-blue-700', purple: 'bg-purple-100 text-purple-700', cyan: 'bg-cyan-100 text-cyan-700', orange: 'bg-orange-100 text-orange-700', teal: 'bg-teal-100 text-teal-700' }; return <span className={`inline-flex px-2 py-0.5 rounded-full text-xs font-medium ${colors[color] || colors.gray}`}>{children}</span>; }

// ---- PR Line Item ----
function PRLineRow({ item, index, onChange, onRemove }) {
  const fc = (f) => (e) => onChange(index, { ...item, [f]: e.target.value });
  return (
    <div className="grid grid-cols-12 gap-2 items-end">
      <div className="col-span-4"><Field label="Description"><Input value={item.description || ''} onChange={fc('description')} placeholder="Sand & gravel mix" /></Field></div>
      <div className="col-span-2"><Field label="Quantity"><Input type="number" step="0.01" value={item.quantity || ''} onChange={fc('quantity')} /></Field></div>
      <div className="col-span-2"><Field label="Unit"><Input value={item.unit || ''} onChange={fc('unit')} placeholder="tonnes" /></Field></div>
      <div className="col-span-3"><Field label="Est. Unit Price (£)"><Input type="number" step="0.01" value={item.estimatedUnitPrice || ''} onChange={fc('estimatedUnitPrice')} /></Field></div>
      <div className="col-span-1 flex justify-end"><button onClick={() => onRemove(index)} className="mb-1 px-2 py-1 text-xs text-red-500 border border-red-200 rounded hover:bg-red-50">X</button></div>
    </div>
  );
}

// ---- PO Line Item ----
function POLineRow({ item, index, onChange, onRemove }) {
  const fc = (f) => (e) => onChange(index, { ...item, [f]: e.target.value });
  return (
    <div className="grid grid-cols-12 gap-2 items-end">
      <div className="col-span-4"><Field label="Description"><Input value={item.description || ''} onChange={fc('description')} /></Field></div>
      <div className="col-span-2"><Field label="Qty"><Input type="number" step="0.01" value={item.quantity || ''} onChange={fc('quantity')} /></Field></div>
      <div className="col-span-2"><Field label="Unit"><Input value={item.unit || ''} onChange={fc('unit')} /></Field></div>
      <div className="col-span-2"><Field label="Unit Price (£)"><Input type="number" step="0.01" value={item.unitPrice || ''} onChange={fc('unitPrice')} /></Field></div>
      <div className="col-span-2 flex justify-between items-center"><span className="text-xs text-slate-500">Total:</span><span className="text-sm font-medium">{item.totalPrice != null ? `£${item.totalPrice}` : '—'}</span><button onClick={() => onRemove(index)} className="ml-1 px-1 text-xs text-red-500 border border-red-200 rounded hover:bg-red-50">X</button></div>
    </div>
  );
}

// ---- Purchase Requisition Form ----
function PRForm({ pr, onSubmit, onClose }) {
  const [form, setForm] = useState(pr || { siteId: '', siteName: '', requestedBy: '', notes: '', lineItems: [{ description: '', quantity: '', unit: '', estimatedUnitPrice: '' }] });
  const [submitting, setSubmitting] = useState(false);

  const setLine = (i, item) => { const lines = [...form.lineItems]; lines[i] = item; setForm({ ...form, lineItems: lines }); };
  const addLine = () => setForm({ ...form, lineItems: [...form.lineItems, { description: '', quantity: '', unit: '', estimatedUnitPrice: '' }] });
  const removeLine = (i) => { if (form.lineItems.length > 1) { const lines = form.lineItems.filter((_, idx) => idx !== i); setForm({ ...form, lineItems: lines }); } };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      const payload = { ...form };
      if (form.siteId) payload.siteId = parseInt(form.siteId);
      payload.lineItems = form.lineItems.map(l => ({ ...l, quantity: l.quantity ? parseFloat(l.quantity) : 0, estimatedUnitPrice: l.estimatedUnitPrice ? parseFloat(l.estimatedUnitPrice) : 0 }));
      await onSubmit(payload);
      onClose();
    } catch (err) { alert(err.response?.data?.message || 'Failed'); }
    finally { setSubmitting(false); }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div className="grid grid-cols-2 gap-4">
        <Field label="Site ID *"><Input type="number" value={form.siteId} onChange={e => setForm({ ...form, siteId: e.target.value })} placeholder="Site database ID" required /></Field>
        <Field label="Requested By"><Input value={form.requestedBy} onChange={e => setForm({ ...form, requestedBy: e.target.value })} placeholder="Name" /></Field>
      </div>
      <Field label="Notes"><Textarea value={form.notes} onChange={e => setForm({ ...form, notes: e.target.value })} rows={2} /></Field>

      <div className="border-t border-gray-200 pt-4">
        <div className="flex justify-between items-center mb-3">
          <h4 className="text-sm font-semibold text-slate-600">Line Items</h4>
          <button type="button" onClick={addLine} className="px-3 py-1 text-xs border rounded hover:bg-gray-50">+ Add Item</button>
        </div>
        <div className="space-y-3">
          {form.lineItems.map((item, i) => <PRLineRow key={i} item={item} index={i} onChange={setLine} onRemove={removeLine} />)}
        </div>
      </div>

      <div className="flex justify-end gap-3 pt-2">
        <button type="button" onClick={onClose} className="px-4 py-2 text-sm text-slate-600 border rounded-lg hover:bg-gray-50">Cancel</button>
        <button type="submit" disabled={submitting} className="px-4 py-2 text-sm bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50">{submitting ? 'Saving…' : pr ? 'Update' : 'Create Requisition'}</button>
      </div>
    </form>
  );
}

// ---- Purchase Order Form ----
function POForm({ po, onSubmit, onClose }) {
  const [form, setForm] = useState(po || { orderNumber: '', orderDate: '', supplierId: '', supplierName: '', siteId: '', siteName: '', status: 'DRAFT', totalValue: '', deliveryDate: '', deliveryAddress: '', notes: '', lineItems: [{ description: '', quantity: '', unit: '', unitPrice: '', totalPrice: '' }] });
  const [submitting, setSubmitting] = useState(false);

  const setLine = (i, item) => {
    const qty = parseFloat(item.quantity) || 0;
    const price = parseFloat(item.unitPrice) || 0;
    const lines = [...form.lineItems]; lines[i] = { ...item, totalPrice: qty * price }; setForm({ ...form, lineItems: lines });
  };
  const addLine = () => setForm({ ...form, lineItems: [...form.lineItems, { description: '', quantity: '', unit: '', unitPrice: '', totalPrice: '' }] });
  const removeLine = (i) => { if (form.lineItems.length > 1) { const lines = form.lineItems.filter((_, idx) => idx !== i); setForm({ ...form, lineItems: lines }); } };
  const total = form.lineItems.reduce((s, l) => s + ((parseFloat(l.quantity) || 0) * (parseFloat(l.unitPrice) || 0)), 0);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      const payload = { ...form, totalValue: total };
      if (form.supplierId) payload.supplierId = parseInt(form.supplierId);
      if (form.siteId) payload.siteId = parseInt(form.siteId);
      payload.lineItems = form.lineItems.map(l => ({ ...l, quantity: l.quantity ? parseFloat(l.quantity) : 0, unitPrice: l.unitPrice ? parseFloat(l.unitPrice) : 0 }));
      await onSubmit(payload);
      onClose();
    } catch (err) { alert(err.response?.data?.message || 'Failed'); }
    finally { setSubmitting(false); }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div className="grid grid-cols-2 gap-4">
        <Field label="Order Number"><Input value={form.orderNumber} onChange={e => setForm({ ...form, orderNumber: e.target.value })} placeholder="PO-2024-001" /></Field>
        <Field label="Order Date"><Input type="date" value={form.orderDate} onChange={e => setForm({ ...form, orderDate: e.target.value })} /></Field>
      </div>
      <div className="grid grid-cols-2 gap-4">
        <Field label="Supplier ID"><Input type="number" value={form.supplierId} onChange={e => setForm({ ...form, supplierId: e.target.value })} placeholder="Company database ID" /></Field>
        <Field label="Supplier Name"><Input value={form.supplierName} onChange={e => setForm({ ...form, supplierName: e.target.value })} placeholder="Aggregate Industries" /></Field>
      </div>
      <div className="grid grid-cols-2 gap-4">
        <Field label="Site ID"><Input type="number" value={form.siteId} onChange={e => setForm({ ...form, siteId: e.target.value })} placeholder="Site database ID" /></Field>
        <Field label="Site Name"><Input value={form.siteName} onChange={e => setForm({ ...form, siteName: e.target.value })} placeholder="Site Name" /></Field>
      </div>
      <div className="grid grid-cols-3 gap-4">
        <Field label="Status"><Select value={form.status} onChange={e => setForm({ ...form, status: e.target.value })}>{PO_STATUSES.map(s => <option key={s} value={s}>{s}</option>)}</Select></Field>
        <Field label="Delivery Date"><Input type="date" value={form.deliveryDate} onChange={e => setForm({ ...form, deliveryDate: e.target.value })} /></Field>
        <Field label="Total Value (£)"><Input type="number" step="0.01" value={total} readOnly className="bg-gray-50" /></Field>
      </div>
      <Field label="Delivery Address"><Textarea value={form.deliveryAddress} onChange={e => setForm({ ...form, deliveryAddress: e.target.value })} rows={2} /></Field>
      <Field label="Notes"><Textarea value={form.notes} onChange={e => setForm({ ...form, notes: e.target.value })} rows={2} /></Field>

      <div className="border-t border-gray-200 pt-4">
        <div className="flex justify-between items-center mb-3">
          <h4 className="text-sm font-semibold text-slate-600">Line Items</h4>
          <button type="button" onClick={addLine} className="px-3 py-1 text-xs border rounded hover:bg-gray-50">+ Add Item</button>
        </div>
        <div className="space-y-3">
          {form.lineItems.map((item, i) => <POLineRow key={i} item={item} index={i} onChange={setLine} onRemove={removeLine} />)}
        </div>
        <div className="flex justify-end mt-2"><span className="text-sm font-semibold text-slate-800">Total: £{total.toFixed(2)}</span></div>
      </div>

      <div className="flex justify-end gap-3 pt-2">
        <button type="button" onClick={onClose} className="px-4 py-2 text-sm text-slate-600 border rounded-lg hover:bg-gray-50">Cancel</button>
        <button type="submit" disabled={submitting} className="px-4 py-2 text-sm bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50">{submitting ? 'Saving…' : po ? 'Update' : 'Create Order'}</button>
      </div>
    </form>
  );
}

// ---- Delivery Note Form ----
function DNForm({ onSubmit, onClose }) {
  const [form, setForm] = useState({ noteNumber: '', deliveryNoteDate: '', purchaseOrderId: '', supplierName: '', siteName: '', materials: [{ description: '', quantity: '', unit: '', notes: '' }] });
  const [submitting, setSubmitting] = useState(false);
  const setMat = (i, item) => { const mats = [...form.materials]; mats[i] = item; setForm({ ...form, materials: mats }); };
  const addMat = () => setForm({ ...form, materials: [...form.materials, { description: '', quantity: '', unit: '', notes: '' }] });
  const removeMat = (i) => { if (form.materials.length > 1) { const mats = form.materials.filter((_, idx) => idx !== i); setForm({ ...form, materials: mats }); } };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try { await onSubmit(form); onClose(); } catch (err) { alert(err.response?.data?.message || 'Failed'); } finally { setSubmitting(false); }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div className="grid grid-cols-2 gap-4">
        <Field label="Note Number"><Input value={form.noteNumber} onChange={e => setForm({ ...form, noteNumber: e.target.value })} placeholder="DN-001" /></Field>
        <Field label="Delivery Date *"><Input type="date" value={form.deliveryNoteDate} onChange={e => setForm({ ...form, deliveryNoteDate: e.target.value })} required /></Field>
      </div>
      <div className="grid grid-cols-2 gap-4">
        <Field label="PO Reference ID"><Input type="number" value={form.purchaseOrderId} onChange={e => setForm({ ...form, purchaseOrderId: e.target.value })} placeholder="PO database ID" /></Field>
        <Field label="Supplier Name"><Input value={form.supplierName} onChange={e => setForm({ ...form, supplierName: e.target.value })} placeholder="Supplier" /></Field>
      </div>
      <Field label="Site Name"><Input value={form.siteName} onChange={e => setForm({ ...form, siteName: e.target.value })} placeholder="Site Name" /></Field>

      <div className="border-t border-gray-200 pt-4">
        <div className="flex justify-between items-center mb-3"><h4 className="text-sm font-semibold text-slate-600">Materials</h4><button type="button" onClick={addMat} className="px-3 py-1 text-xs border rounded hover:bg-gray-50">+ Add</button></div>
        {form.materials.map((m, i) => (
          <div key={i} className="grid grid-cols-12 gap-2 mb-2 items-end">
            <div className="col-span-5"><Field label="Description"><Input value={m.description} onChange={e => setMat(i, { ...m, description: e.target.value })} /></Field></div>
            <div className="col-span-2"><Field label="Qty"><Input type="number" step="0.01" value={m.quantity} onChange={e => setMat(i, { ...m, quantity: e.target.value })} /></Field></div>
            <div className="col-span-2"><Field label="Unit"><Input value={m.unit} onChange={e => setMat(i, { ...m, unit: e.target.value })} /></Field></div>
            <div className="col-span-3 flex items-end"><button type="button" onClick={() => removeMat(i)} className="px-2 py-1 text-xs text-red-500 border border-red-200 rounded hover:bg-red-50">X</button></div>
          </div>
        ))}
      </div>
      <div className="flex justify-end gap-3 pt-2">
        <button type="button" onClick={onClose} className="px-4 py-2 text-sm text-slate-600 border rounded-lg hover:bg-gray-50">Cancel</button>
        <button type="submit" disabled={submitting} className="px-4 py-2 text-sm bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50">{submitting ? 'Saving…' : 'Create Delivery Note'}</button>
      </div>
    </form>
  );
}

// ---- Concrete Ticket Form ----
function ConcreteForm({ onSubmit, onClose }) {
  const [form, setForm] = useState({ ticketNumber: '', ticketDate: '', supplier: '', mixDesign: '', cubicMetres: '', slump: '', timeOfDelivery: '', siteName: '', contractRef: '', notes: '' });
  const [submitting, setSubmitting] = useState(false);
  const fc = (f) => (e) => setForm({ ...form, [f]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      const payload = { ...form };
      if (form.cubicMetres) payload.cubicMetres = parseFloat(form.cubicMetres);
      await onSubmit(payload);
      onClose();
    } catch (err) { alert(err.response?.data?.message || 'Failed'); } finally { setSubmitting(false); }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div className="grid grid-cols-2 gap-4">
        <Field label="Ticket Number *"><Input value={form.ticketNumber} onChange={fc('ticketNumber')} required placeholder="CT-001" /></Field>
        <Field label="Ticket Date *"><Input type="date" value={form.ticketDate} onChange={fc('ticketDate')} required /></Field>
      </div>
      <div className="grid grid-cols-2 gap-4">
        <Field label="Supplier"><Input value={form.supplier} onChange={fc('supplier')} placeholder="Concrete Co. Ltd" /></Field>
        <Field label="Mix Design"><Input value={form.mixDesign} onChange={fc('mixDesign')} placeholder="C30/37 GEN0" /></Field>
      </div>
      <div className="grid grid-cols-3 gap-4">
        <Field label="Cubic Metres *"><Input type="number" step="0.01" value={form.cubicMetres} onChange={fc('cubicMetres')} required /></Field>
        <Field label="Slump (mm)"><Input value={form.slump} onChange={fc('slump')} placeholder="150" /></Field>
        <Field label="Time of Delivery"><Input type="time" value={form.timeOfDelivery} onChange={fc('timeOfDelivery')} /></Field>
      </div>
      <div className="grid grid-cols-2 gap-4">
        <Field label="Site Name"><Input value={form.siteName} onChange={fc('siteName')} /></Field>
        <Field label="Contract Ref"><Input value={form.contractRef} onChange={fc('contractRef')} /></Field>
      </div>
      <Field label="Notes"><Textarea value={form.notes} onChange={fc('notes')} rows={2} /></Field>
      <div className="flex justify-end gap-3 pt-2">
        <button type="button" onClick={onClose} className="px-4 py-2 text-sm text-slate-600 border rounded-lg hover:bg-gray-50">Cancel</button>
        <button type="submit" disabled={submitting} className="px-4 py-2 text-sm bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50">{submitting ? 'Saving…' : 'Create Ticket'}</button>
      </div>
    </form>
  );
}

// ---- Muckaway Ticket Form ----
function MuckawayForm({ onSubmit, onClose }) {
  const [form, setForm] = useState({ ticketNumber: '', ticketDate: '', siteName: '', wasteType: '', tonnage: '', carrierName: '', licenceNumber: '', destinationSite: '', notes: '' });
  const [submitting] = useState(false);
  const fc = (f) => (e) => setForm({ ...form, [f]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try { await onSubmit(form); onClose(); } catch (err) { alert(err.response?.data?.message || 'Failed'); } finally { setSubmitting(false); }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div className="grid grid-cols-2 gap-4">
        <Field label="Ticket Number *"><Input value={form.ticketNumber} onChange={fc('ticketNumber')} required placeholder="MT-001" /></Field>
        <Field label="Ticket Date *"><Input type="date" value={form.ticketDate} onChange={fc('ticketDate')} required /></Field>
      </div>
      <Field label="Site Name"><Input value={form.siteName} onChange={fc('siteName')} /></Field>
      <div className="grid grid-cols-2 gap-4">
        <Field label="Waste Type"><Input value={form.wasteType} onChange={fc('wasteType')} placeholder="Excavated Soil" /></Field>
        <Field label="Tonnage (tonnes) *"><Input type="number" step="0.01" value={form.tonnage} onChange={fc('tonnage')} required /></Field>
      </div>
      <div className="grid grid-cols-2 gap-4">
        <Field label="Carrier Name"><Input value={form.carrierName} onChange={fc('carrierName')} placeholder="ABC Transport" /></Field>
        <Field label="Licence Number"><Input value={form.licenceNumber} onChange={fc('licenceNumber')} placeholder="ENV/12345" /></Field>
      </div>
      <Field label="Destination Site"><Input value={form.destinationSite} onChange={fc('destinationSite')} placeholder="Waste Management Facility" /></Field>
      <Field label="Notes"><Textarea value={form.notes} onChange={fc('notes')} rows={2} /></Field>
      <div className="flex justify-end gap-3 pt-2">
        <button type="button" onClick={onClose} className="px-4 py-2 text-sm text-slate-600 border rounded-lg hover:bg-gray-50">Cancel</button>
        <button type="submit" disabled={submitting} className="px-4 py-2 text-sm bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50">{submitting ? 'Saving…' : 'Create Ticket'}</button>
      </div>
    </form>
  );
}

// ---- Detail Modals ----
function PODetail({ po, onClose, onDelete }) {
  return (
    <Modal title={`Purchase Order: ${po.orderNumber || po.id}`} onClose={onClose} size="max-w-3xl">
      <div className="space-y-1">
        {[['Order Number', po.orderNumber], ['Date', po.orderDate], ['Supplier', po.supplierName], ['Site', po.siteName], ['Status', po.status], ['Total Value', po.totalValue ? `£${po.totalValue}` : null], ['Delivery Date', po.deliveryDate], ['Delivery Address', po.deliveryAddress]].filter(([, v]) => v).map(([label, value]) => (
          <div key={label} className="flex justify-between py-2 border-b border-gray-50 last:border-0">
            <span className="text-sm text-slate-500">{label}</span>
            <span className="text-sm text-slate-800 font-medium text-right">{label === 'Status' ? <Badge color={po.status === 'RECEIVED' ? 'teal' : po.status === 'APPROVED' ? 'green' : po.status === 'CANCELLED' ? 'red' : 'blue'}>{value}</Badge> : value}</span>
          </div>
        ))}
      </div>
      {po.lineItems && po.lineItems.length > 0 && (
        <div className="mt-4">
          <h4 className="text-sm font-semibold text-slate-600 mb-2">Line Items</h4>
          <table className="w-full text-sm">
            <thead><tr className="bg-slate-50"><th className="text-left px-3 py-2 text-xs text-slate-500">Description</th><th className="text-right px-3 py-2 text-xs text-slate-500">Qty</th><th className="text-right px-3 py-2 text-xs text-slate-500">Unit Price</th><th className="text-right px-3 py-2 text-xs text-slate-500">Total</th></tr></thead>
            <tbody>{po.lineItems.map((l, i) => <tr key={i} className="border-b border-gray-50"><td className="px-3 py-2">{l.description}</td><td className="px-3 py-2 text-right">{l.quantity} {l.unit}</td><td className="px-3 py-2 text-right">{l.unitPrice != null ? `£${l.unitPrice}` : '—'}</td><td className="px-3 py-2 text-right font-medium">{l.totalPrice != null ? `£${l.totalPrice}` : '—'}</td></tr>)}</tbody>
          </table>
        </div>
      )}
      {po.notes && <div className="mt-3 p-3 bg-slate-50 rounded-lg text-sm text-slate-600">{po.notes}</div>}
      <div className="flex justify-between mt-4 pt-3 border-t">
        <button onClick={() => { if (confirm('Delete this PO?')) { onDelete(po.id); onClose(); } }} className="px-4 py-2 text-xs border border-red-200 text-red-600 rounded-lg hover:bg-red-50">Delete</button>
        <button onClick={onClose} className="px-4 py-2 text-sm text-slate-600 border rounded-lg hover:bg-gray-50">Close</button>
      </div>
    </Modal>
  );
}

function DNDetail({ dn, onClose }) {
  return (
    <Modal title={`Delivery Note: ${dn.noteNumber || dn.id}`} onClose={onClose}>
      <div className="space-y-1">
        {[['Note Number', dn.noteNumber], ['Date', dn.deliveryNoteDate], ['PO Ref', dn.purchaseOrderId], ['Supplier', dn.supplierName], ['Site', dn.siteName]].filter(([, v]) => v).map(([label, value]) => (
          <div key={label} className="flex justify-between py-2 border-b border-gray-50 last:border-0">
            <span className="text-sm text-slate-500">{label}</span><span className="text-sm text-slate-800 font-medium">{value}</span>
          </div>
        ))}
      </div>
      {dn.materials && dn.materials.length > 0 && (
        <div className="mt-4">
          <h4 className="text-sm font-semibold text-slate-600 mb-2">Materials</h4>
          <table className="w-full text-sm">
            <thead><tr className="bg-slate-50"><th className="text-left px-3 py-2 text-xs text-slate-500">Description</th><th className="text-right px-3 py-2 text-xs text-slate-500">Qty</th><th className="text-left px-3 py-2 text-xs text-slate-500">Notes</th></tr></thead>
            <tbody>{dn.materials.map((m, i) => <tr key={i} className="border-b border-gray-50"><td className="px-3 py-2">{m.description}</td><td className="px-3 py-2 text-right">{m.quantity} {m.unit}</td><td className="px-3 py-2 text-xs text-slate-400">{m.notes || '—'}</td></tr>)}</tbody>
          </table>
        </div>
      )}
      <div className="mt-4"><button onClick={onClose} className="px-4 py-2 text-sm text-slate-600 border rounded-lg hover:bg-gray-50">Close</button></div>
    </Modal>
  );
}

function ConcreteDetail({ ct, onClose }) {
  return (
    <Modal title={`Concrete Ticket: ${ct.ticketNumber}`} onClose={onClose}>
      <div className="grid grid-cols-2 gap-4">
        {[['Ticket Number', ct.ticketNumber], ['Date', ct.ticketDate], ['Supplier', ct.supplier], ['Mix Design', ct.mixDesign], ['Cubic Metres', ct.cubicMetres ? `${ct.cubicMetres} m³` : null], ['Slump', ct.slump ? `${ct.slump} mm` : null], ['Time of Delivery', ct.timeOfDelivery], ['Site', ct.siteName], ['Contract', ct.contractRef]].filter(([, v]) => v).map(([label, value]) => (
          <div key={label}><span className="text-xs text-slate-500">{label}</span><p className="text-sm font-medium text-slate-800">{value}</p></div>
        ))}
      </div>
      {ct.notes && <div className="mt-3 p-3 bg-slate-50 rounded-lg text-sm text-slate-600">{ct.notes}</div>}
      <div className="mt-4"><button onClick={onClose} className="px-4 py-2 text-sm text-slate-600 border rounded-lg hover:bg-gray-50">Close</button></div>
    </Modal>
  );
}

function MuckawayDetail({ mt, onClose }) {
  return (
    <Modal title={`Muckaway Ticket: ${mt.ticketNumber}`} onClose={onClose}>
      <div className="grid grid-cols-2 gap-4">
        {[['Ticket Number', mt.ticketNumber], ['Date', mt.ticketDate], ['Site', mt.siteName], ['Waste Type', mt.wasteType], ['Tonnage', mt.tonnage ? `${mt.tonnage} tonnes` : null], ['Carrier', mt.carrierName], ['Licence', mt.licenceNumber], ['Destination', mt.destinationSite]].filter(([, v]) => v).map(([label, value]) => (
          <div key={label}><span className="text-xs text-slate-500">{label}</span><p className="text-sm font-medium text-slate-800">{value}</p></div>
        ))}
      </div>
      {mt.notes && <div className="mt-3 p-3 bg-slate-50 rounded-lg text-sm text-slate-600">{mt.notes}</div>}
      <div className="mt-4"><button onClick={onClose} className="px-4 py-2 text-sm text-slate-600 border rounded-lg hover:bg-gray-50">Close</button></div>
    </Modal>
  );
}

// ---- Tab Components ----
function POTab() {
  const [pos, setPOs] = useState([]); const [loading, setLoading] = useState(true);
  const [totalPages, setTotalPages] = useState(1); const [page, setPage] = useState(0);
  const [statusFilter, setStatusFilter] = useState(''); const [search, setSearch] = useState('');
  const [showForm, setShowForm] = useState(false); const [detail, setDetail] = useState(null);

  const fetch = async (p = 0, st = statusFilter) => {
    setLoading(true);
    try {
      const params = new URLSearchParams({ page: p, size: 20 });
      if (st) params.append('status', st);
      const res = await axios.get(`${API}/procurement/purchase-orders?${params}`);
      const d = res.data.data?.content || res.data.data || [];
      setPOs(d); setTotalPages(res.data.data?.totalPages || 1); setPage(p);
    } catch { setPOs([]); } finally { setLoading(false); }
  };

  useEffect(() => { fetch(); }, []);
  const debounce = (fn, ms = 400) => { let t; return (...a) => { clearTimeout(t); t = setTimeout(() => fn(...a), ms); }; };
  const dbFetch = debounce((s) => fetch(0, statusFilter), 400);

  const handleCreate = async (form) => { await axios.post(`${API}/procurement/purchase-orders`, form); fetch(page, statusFilter); };
  const handleDelete = async (id) => { await axios.delete(`${API}/procurement/purchase-orders/${id}`); fetch(page, statusFilter); };

  const totalValue = pos.reduce((s, p) => s + (parseFloat(p.totalValue) || 0), 0);

  return (
    <div>
      <div className="flex justify-between items-center mb-4">
        <div className="flex gap-3 flex-1">
          <input value={search} onChange={e => { setSearch(e.target.value); dbFetch(e.target.value); }} placeholder="Search orders…" className="px-4 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 flex-1" />
          <select value={statusFilter} onChange={e => { setStatusFilter(e.target.value); fetch(0, e.target.value); }} className="px-3 py-2 border border-gray-300 rounded-lg text-sm bg-white">
            <option value="">All Status</option>{PO_STATUSES.map(s => <option key={s} value={s}>{s}</option>)}
          </select>
        </div>
        <button onClick={() => setShowForm(true)} className="ml-3 px-4 py-2 bg-blue-600 text-white text-sm rounded-lg hover:bg-blue-700">+ New PO</button>
      </div>

      <div className="grid grid-cols-3 gap-3 mb-4">
        <div className="bg-blue-50 rounded-xl p-3"><p className="text-xs text-blue-500">Total Orders</p><p className="text-xl font-bold text-slate-800">{pos.length}</p></div>
        <div className="bg-teal-50 rounded-xl p-3"><p className="text-xs text-teal-500">Total Value</p><p className="text-xl font-bold text-slate-800">£{totalValue.toFixed(0)}</p></div>
        <div className="bg-green-50 rounded-xl p-3"><p className="text-xs text-green-500">Received</p><p className="text-xl font-bold text-slate-800">{pos.filter(p => p.status === 'RECEIVED').length}</p></div>
      </div>

      <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead><tr className="bg-slate-50 border-b border-gray-100">
              <th className="text-left px-5 py-3 text-xs font-semibold text-slate-500 uppercase tracking-wider">Order No.</th>
              <th className="text-left px-5 py-3 text-xs font-semibold text-slate-500 uppercase tracking-wider">Date</th>
              <th className="text-left px-5 py-3 text-xs font-semibold text-slate-500 uppercase tracking-wider">Supplier</th>
              <th className="text-left px-5 py-3 text-xs font-semibold text-slate-500 uppercase tracking-wider">Site</th>
              <th className="text-left px-5 py-3 text-xs font-semibold text-slate-500 uppercase tracking-wider">Status</th>
              <th className="text-right px-5 py-3 text-xs font-semibold text-slate-500 uppercase tracking-wider">Total</th>
              <th className="text-right px-5 py-3 text-xs font-semibold text-slate-500 uppercase tracking-wider">Actions</th>
            </tr></thead>
            <tbody>
              {loading ? <tr><td colSpan={7} className="text-center py-8 text-slate-400">Loading…</td></tr> : pos.length === 0 ? <tr><td colSpan={7} className="text-center py-8 text-slate-400">No purchase orders found</td></tr> :
                pos.map(p => (
                  <tr key={p.id} className="border-b border-gray-50 hover:bg-slate-50 transition-colors">
                    <td className="px-5 py-4 text-sm font-medium text-slate-800">{p.orderNumber || p.id}</td>
                    <td className="px-5 py-4 text-sm text-slate-600">{p.orderDate || '—'}</td>
                    <td className="px-5 py-4 text-sm text-slate-600">{p.supplierName || '—'}</td>
                    <td className="px-5 py-4 text-sm text-slate-600">{p.siteName || '—'}</td>
                    <td className="px-5 py-4"><Badge color={p.status === 'RECEIVED' ? 'teal' : p.status === 'APPROVED' ? 'green' : p.status === 'CANCELLED' ? 'red' : p.status === 'PENDING' ? 'amber' : 'blue'}>{p.status || '—'}</Badge></td>
                    <td className="px-5 py-4 text-sm text-slate-800 text-right font-medium">{p.totalValue ? `£${p.totalValue}` : '—'}</td>
                    <td className="px-5 py-4 text-right"><button onClick={() => setDetail(p)} className="px-3 py-1 text-xs border rounded hover:bg-gray-50">View</button></td>
                  </tr>
                ))}
            </tbody>
          </table>
        </div>
        {totalPages > 1 && <div className="flex justify-between items-center px-5 py-3 border-t border-gray-100">
          <p className="text-xs text-slate-500">Page {page + 1} of {totalPages}</p>
          <div className="flex gap-1"><button disabled={page === 0} onClick={() => fetch(page - 1)} className="px-3 py-1 text-xs border rounded disabled:opacity-30 hover:bg-gray-50">Prev</button><button disabled={page >= totalPages - 1} onClick={() => fetch(page + 1)} className="px-3 py-1 text-xs border rounded disabled:opacity-30 hover:bg-gray-50">Next</button></div>
        </div>}
      </div>
      {showForm && <Modal title="New Purchase Order" onClose={() => setShowForm(false)} size="max-w-3xl"><POForm onSubmit={handleCreate} onClose={() => setShowForm(false)} /></Modal>}
      {detail && <PODetail po={detail} onClose={() => setDetail(null)} onDelete={handleDelete} />}
    </div>
  );
}

function DNTab() {
  const [dns, setDNs] = useState([]); const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false); const [detail, setDetail] = useState(null);

  const fetch = async () => {
    setLoading(true);
    try { const res = await axios.get(`${API}/procurement/delivery-notes`); setDNs(res.data.data || []); }
    catch { setDNs([]); } finally { setLoading(false); }
  };

  useEffect(() => { fetch(); }, []);

  const handleCreate = async (form) => { await axios.post(`${API}/procurement/delivery-notes`, form); fetch(); };

  return (
    <div>
      <div className="flex justify-between mb-4"><input placeholder="Search delivery notes…" className="px-4 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 flex-1" /><button onClick={() => setShowForm(true)} className="ml-3 px-4 py-2 bg-blue-600 text-white text-sm rounded-lg hover:bg-blue-700">+ New Delivery Note</button></div>
      <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
        <table className="w-full">
          <thead><tr className="bg-slate-50 border-b border-gray-100"><th className="text-left px-5 py-3 text-xs font-semibold text-slate-500 uppercase">Note No.</th><th className="text-left px-5 py-3 text-xs font-semibold text-slate-500 uppercase">Date</th><th className="text-left px-5 py-3 text-xs font-semibold text-slate-500 uppercase">Supplier</th><th className="text-left px-5 py-3 text-xs font-semibold text-slate-500 uppercase">Site</th><th className="text-right px-5 py-3 text-xs font-semibold text-slate-500 uppercase">Actions</th></tr></thead>
          <tbody>
            {loading ? <tr><td colSpan={5} className="text-center py-8 text-slate-400">Loading…</td></tr> : dns.length === 0 ? <tr><td colSpan={5} className="text-center py-8 text-slate-400">No delivery notes found</td></tr> :
              dns.map(d => <tr key={d.id} className="border-b border-gray-50 hover:bg-slate-50 transition-colors"><td className="px-5 py-4 text-sm font-medium text-slate-800">{d.noteNumber || d.id}</td><td className="px-5 py-4 text-sm text-slate-600">{d.deliveryNoteDate || '—'}</td><td className="px-5 py-4 text-sm text-slate-600">{d.supplierName || '—'}</td><td className="px-5 py-4 text-sm text-slate-600">{d.siteName || '—'}</td><td className="px-5 py-4 text-right"><button onClick={() => setDetail(d)} className="px-3 py-1 text-xs border rounded hover:bg-gray-50">View</button></td></tr>)}
          </tbody>
        </table>
      </div>
      {showForm && <Modal title="New Delivery Note" onClose={() => setShowForm(false)} size="max-w-2xl"><DNForm onSubmit={handleCreate} onClose={() => setShowForm(false)} /></Modal>}
      {detail && <DNDetail dn={detail} onClose={() => setDetail(null)} />}
    </div>
  );
}

function ConcreteTab() {
  const [tickets, setTickets] = useState([]); const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false); const [detail, setDetail] = useState(null);

  const fetch = async () => { setLoading(true); try { const res = await axios.get(`${API}/concrete-tickets`); setTickets(res.data.data?.content || res.data.data || []); } catch { setTickets([]); } finally { setLoading(false); } };
  useEffect(() => { fetch(); }, []);
  const handleCreate = async (form) => { await axios.post(`${API}/concrete-tickets`, form); fetch(); };

  const totalM3 = tickets.reduce((s, t) => s + (parseFloat(t.cubicMetres) || 0), 0);

  return (
    <div>
      <div className="flex justify-between mb-4"><div className="flex gap-3"><div className="bg-blue-50 rounded-xl p-3"><p className="text-xs text-blue-500">Tickets</p><p className="text-xl font-bold text-slate-800">{tickets.length}</p></div><div className="bg-teal-50 rounded-xl p-3"><p className="text-xs text-teal-500">Total Volume</p><p className="text-xl font-bold text-slate-800">{totalM3.toFixed(1)} m³</p></div></div><button onClick={() => setShowForm(true)} className="px-4 py-2 bg-blue-600 text-white text-sm rounded-lg hover:bg-blue-700">+ New Ticket</button></div>
      <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
        <table className="w-full">
          <thead><tr className="bg-slate-50 border-b border-gray-100"><th className="text-left px-5 py-3 text-xs font-semibold text-slate-500 uppercase">Ticket No.</th><th className="text-left px-5 py-3 text-xs font-semibold text-slate-500 uppercase">Date</th><th className="text-left px-5 py-3 text-xs font-semibold text-slate-500 uppercase">Supplier</th><th className="text-left px-5 py-3 text-xs font-semibold text-slate-500 uppercase">Mix Design</th><th className="text-right px-5 py-3 text-xs font-semibold text-slate-500 uppercase">Volume</th><th className="text-left px-5 py-3 text-xs font-semibold text-slate-500 uppercase">Site</th><th className="text-right px-5 py-3 text-xs font-semibold text-slate-500 uppercase">Actions</th></tr></thead>
          <tbody>
            {loading ? <tr><td colSpan={7} className="text-center py-8 text-slate-400">Loading…</td></tr> : tickets.length === 0 ? <tr><td colSpan={7} className="text-center py-8 text-slate-400">No concrete tickets found</td></tr> :
              tickets.map(t => <tr key={t.id} className="border-b border-gray-50 hover:bg-slate-50 transition-colors"><td className="px-5 py-4 text-sm font-medium text-slate-800">{t.ticketNumber}</td><td className="px-5 py-4 text-sm text-slate-600">{t.ticketDate}</td><td className="px-5 py-4 text-sm text-slate-600">{t.supplier}</td><td className="px-5 py-4 text-sm text-slate-600">{t.mixDesign}</td><td className="px-5 py-4 text-sm text-slate-800 text-right font-medium">{t.cubicMetres} m³</td><td className="px-5 py-4 text-sm text-slate-600">{t.siteName}</td><td className="px-5 py-4 text-right"><button onClick={() => setDetail(t)} className="px-3 py-1 text-xs border rounded hover:bg-gray-50">View</button></td></tr>)}
          </tbody>
        </table>
      </div>
      {showForm && <Modal title="New Concrete Ticket" onClose={() => setShowForm(false)}><ConcreteForm onSubmit={handleCreate} onClose={() => setShowForm(false)} /></Modal>}
      {detail && <ConcreteDetail ct={detail} onClose={() => setDetail(null)} />}
    </div>
  );
}

function MuckawayTab() {
  const [tickets, setTickets] = useState([]); const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false); const [detail, setDetail] = useState(null);

  const fetch = async () => { setLoading(true); try { const res = await axios.get(`${API}/muckaway-tickets`); setTickets(res.data.data?.content || res.data.data || []); } catch { setTickets([]); } finally { setLoading(false); } };
  useEffect(() => { fetch(); }, []);
  const handleCreate = async (form) => { await axios.post(`${API}/muckaway-tickets`, form); fetch(); };

  const totalT = tickets.reduce((s, t) => s + (parseFloat(t.tonnage) || 0), 0);

  return (
    <div>
      <div className="flex justify-between mb-4"><div className="flex gap-3"><div className="bg-orange-50 rounded-xl p-3"><p className="text-xs text-orange-500">Tickets</p><p className="text-xl font-bold text-slate-800">{tickets.length}</p></div><div className="bg-amber-50 rounded-xl p-3"><p className="text-xs text-amber-500">Total Tonnage</p><p className="text-xl font-bold text-slate-800">{totalT.toFixed(1)} t</p></div></div><button onClick={() => setShowForm(true)} className="px-4 py-2 bg-blue-600 text-white text-sm rounded-lg hover:bg-blue-700">+ New Ticket</button></div>
      <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
        <table className="w-full">
          <thead><tr className="bg-slate-50 border-b border-gray-100"><th className="text-left px-5 py-3 text-xs font-semibold text-slate-500 uppercase">Ticket No.</th><th className="text-left px-5 py-3 text-xs font-semibold text-slate-500 uppercase">Date</th><th className="text-left px-5 py-3 text-xs font-semibold text-slate-500 uppercase">Site</th><th className="text-left px-5 py-3 text-xs font-semibold text-slate-500 uppercase">Waste Type</th><th className="text-right px-5 py-3 text-xs font-semibold text-slate-500 uppercase">Tonnage</th><th className="text-left px-5 py-3 text-xs font-semibold text-slate-500 uppercase">Carrier</th><th className="text-right px-5 py-3 text-xs font-semibold text-slate-500 uppercase">Actions</th></tr></thead>
          <tbody>
            {loading ? <tr><td colSpan={7} className="text-center py-8 text-slate-400">Loading…</td></tr> : tickets.length === 0 ? <tr><td colSpan={7} className="text-center py-8 text-slate-400">No muckaway tickets found</td></tr> :
              tickets.map(t => <tr key={t.id} className="border-b border-gray-50 hover:bg-slate-50 transition-colors"><td className="px-5 py-4 text-sm font-medium text-slate-800">{t.ticketNumber}</td><td className="px-5 py-4 text-sm text-slate-600">{t.ticketDate}</td><td className="px-5 py-4 text-sm text-slate-600">{t.siteName}</td><td className="px-5 py-4 text-sm text-slate-600">{t.wasteType}</td><td className="px-5 py-4 text-sm text-slate-800 text-right font-medium">{t.tonnage} t</td><td className="px-5 py-4 text-sm text-slate-600">{t.carrierName}</td><td className="px-5 py-4 text-right"><button onClick={() => setDetail(t)} className="px-3 py-1 text-xs border rounded hover:bg-gray-50">View</button></td></tr>)}
          </tbody>
        </table>
      </div>
      {showForm && <Modal title="New Muckaway Ticket" onClose={() => setShowForm(false)}><MuckawayForm onSubmit={handleCreate} onClose={() => setShowForm(false)} /></Modal>}
      {detail && <MuckawayDetail mt={detail} onClose={() => setDetail(null)} />}
    </div>
  );
}

// ---- Main Page ----
export default function Materials() {
  const [tab, setTab] = useState('purchase-orders');
  const TABS = [
    { key: 'purchase-orders', label: 'Purchase Orders', icon: '📋' },
    { key: 'delivery-notes', label: 'Delivery Notes', icon: '📦' },
    { key: 'concrete', label: 'Concrete Tickets', icon: '🧱' },
    { key: 'muckaway', label: 'Muckaway', icon: '🚛' },
  ];

  return (
    <div className="p-8">
      <div className="flex justify-between items-center mb-6">
        <div>
          <h2 className="text-2xl font-bold text-slate-800">Materials & Procurement</h2>
          <p className="text-sm text-slate-500 mt-1">Purchase orders, delivery notes, concrete and muckaway tickets</p>
        </div>
      </div>

      <div className="flex gap-1 mb-6 bg-slate-100 p-1 rounded-lg w-fit">
        {TABS.map(t => (
          <button key={t.key} onClick={() => setTab(t.key)}
            className={`px-4 py-2 text-sm font-medium rounded-md transition-colors flex items-center gap-2 ${tab === t.key ? 'bg-white text-slate-800 shadow-sm' : 'text-slate-500 hover:text-slate-700'}`}>
            <span>{t.icon}</span>{t.label}
          </button>
        ))}
      </div>

      {tab === 'purchase-orders' && <POTab />}
      {tab === 'delivery-notes' && <DNTab />}
      {tab === 'concrete' && <ConcreteTab />}
      {tab === 'muckaway' && <MuckawayTab />}
    </div>
  );
}