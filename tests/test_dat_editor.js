/* Testes unitários do codec do editor, usando apenas dados fictícios.
 * Nenhum DAT, inventário, conta ou APK privado participa da CI.
 */
'use strict';
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');

const html = fs.readFileSync(path.join(__dirname, '..', 'assets', 'editor.html'), 'utf8');
const start = html.indexOf('function decodeMUTF8(');
const middle = html.indexOf('function cloneEntries(', start);
const patchStart = html.indexOf('function encodeStringRecord(');
const patchEnd = html.indexOf("$('#export').onclick=", patchStart);
assert.ok(start >= 0 && middle > start && patchStart > middle && patchEnd > patchStart, 'Codec não encontrado no HTML');
const sandbox = { Uint8Array, Number, Error, JSON, Array, Object, Math, String };
vm.createContext(sandbox);
vm.runInContext(html.slice(start, middle) + html.slice(patchStart, patchEnd), sandbox);

function join(arrays) {
  return new Uint8Array(Buffer.concat(arrays.map(a => Buffer.from(a))));
}
const keys = ['hlfeeds', 'hlfeedsquest', 'hlfeedsraid', 'hlfavor', 'hlfavorRoute'];
const arrays = Object.fromEntries(keys.map((key, index) => [key, [
  { name: 'Geral 🌎', minIV: 0, maxIV: 100, distance: index, extra: 'conservar' },
  { name: 'PvP ⚔️', minIV: 90, maxIV: 100, distance: 100 + index }
]]));
const parts = [Uint8Array.from([0xac, 0xed, 0x00, 0x05])];
for (const key of keys) {
  parts.push(sandbox.encodeStringRecord(key));
  parts.push(sandbox.encodeStringRecord(JSON.stringify(arrays[key])));
}
const source = join(parts);
const locations = Object.fromEntries(keys.map(key => [key, sandbox.locateSection(source, key)]));
for (const key of keys) {
  assert.equal(JSON.stringify(locations[key].values), JSON.stringify(arrays[key]));
}
const renamed = JSON.parse(JSON.stringify(arrays.hlfeeds));
renamed[1].name = 'Top 1 🏆 • eficiência 99,5%';
renamed.unshift(renamed.pop());
const reordered = JSON.parse(JSON.stringify(arrays.hlfavor));
reordered.reverse();
const patched = sandbox.patchBytes(source, [
  { from: locations.hlfeeds.tokenPos, to: locations.hlfeeds.end, repl: sandbox.encodeStringRecord(JSON.stringify(renamed)) },
  { from: locations.hlfavor.tokenPos, to: locations.hlfavor.end, repl: sandbox.encodeStringRecord(JSON.stringify(reordered)) }
]);
assert.equal(JSON.stringify(sandbox.locateSection(patched, 'hlfeeds').values), JSON.stringify(renamed));
assert.equal(JSON.stringify(sandbox.locateSection(patched, 'hlfavor').values), JSON.stringify(reordered));
for (const key of keys.filter(k => !['hlfeeds', 'hlfavor'].includes(k))) {
  assert.equal(JSON.stringify(sandbox.locateSection(patched, key).values), JSON.stringify(arrays[key]), key + ' alterada');
}
assert.equal(Buffer.compare(Buffer.from(source.slice(0, 4)), Buffer.from(patched.slice(0, 4))), 0);
const longList = JSON.stringify([{ name: 'X'.repeat(70000) }]);
assert.equal(sandbox.encodeStringRecord(longList)[0], 0x7c, 'Esperado TC_LONGSTRING');
assert.equal(sandbox.locateSection(join([Uint8Array.from([0xac,0xed,0,5]), sandbox.encodeStringRecord('hlfeeds'), sandbox.encodeStringRecord(longList)]), 'hlfeeds').values[0].name.length, 70000);
console.log('OK: 5 seções, reordenação, renomeação Unicode, preservação das demais e long string.');
