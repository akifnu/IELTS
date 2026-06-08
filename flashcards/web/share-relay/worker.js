// Optional Cloudflare Worker for cross-device deck delivery.
// Deploy with: npx wrangler kv namespace create SHINE_SHARE_KV
// Bind KV as SHARE_KV, then: npx wrangler deploy
// Set window.SHINE_SHARE_API to your worker URL in auth-config.js

const CORS = {
  'Access-Control-Allow-Origin': '*',
  'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
  'Access-Control-Allow-Headers': 'Content-Type',
};

function json(data, status = 200) {
  return new Response(JSON.stringify(data), {
    status,
    headers: { ...CORS, 'Content-Type': 'application/json' },
  });
}

async function readInbox(kv, email) {
  const raw = await kv.get(`inbox:${email}`);
  return raw ? JSON.parse(raw) : [];
}

async function writeInbox(kv, email, items) {
  await kv.put(`inbox:${email}`, JSON.stringify(items));
}

export default {
  async fetch(request, env) {
    if (request.method === 'OPTIONS') return new Response(null, { headers: CORS });

    const url = new URL(request.url);
    const kv = env.SHARE_KV;
    if (!kv) return json({ error: 'KV not configured' }, 500);

    if (request.method === 'POST' && url.pathname === '/send') {
      const body = await request.json();
      const to = String(body.to || '').trim().toLowerCase();
      if (!to || !to.includes('@')) return json({ error: 'Invalid recipient' }, 400);
      const entry = {
        id: body.id || `${Date.now()}`,
        fromEmail: body.fromEmail || null,
        fromName: body.fromName || 'Someone',
        deckName: body.deckName || 'Shared deck',
        payload: body.payload,
        createdAt: body.createdAt || new Date().toISOString(),
      };
      if (!entry.payload) return json({ error: 'Missing deck payload' }, 400);
      const inbox = await readInbox(kv, to);
      if (!inbox.some((i) => i.id === entry.id)) inbox.push(entry);
      await writeInbox(kv, to, inbox);
      return json({ ok: true });
    }

    if (request.method === 'GET' && url.pathname === '/inbox') {
      const to = String(url.searchParams.get('to') || '').trim().toLowerCase();
      if (!to) return json({ error: 'Missing to' }, 400);
      return json(await readInbox(kv, to));
    }

    if (request.method === 'POST' && url.pathname === '/dismiss') {
      const body = await request.json();
      const to = String(body.to || '').trim().toLowerCase();
      const id = body.id;
      if (!to || !id) return json({ error: 'Invalid request' }, 400);
      const inbox = (await readInbox(kv, to)).filter((i) => i.id !== id);
      await writeInbox(kv, to, inbox);
      return json({ ok: true });
    }

    return json({ error: 'Not found' }, 404);
  },
};
