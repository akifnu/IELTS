// Optional Cloudflare Worker for cross-device deck delivery and live grants.
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
        fromUserId: body.fromUserId || null,
        deckName: body.deckName || 'Shared deck',
        role: body.role || 'viewer',
        grantId: body.grantId || null,
        accessType: body.accessType || 'granted',
        payload: body.payload,
        createdAt: body.createdAt || new Date().toISOString(),
      };
      if (!entry.payload) return json({ error: 'Missing deck payload' }, 400);
      const inbox = await readInbox(kv, to);
      if (!inbox.some((i) => i.id === entry.id)) inbox.push(entry);
      await writeInbox(kv, to, inbox);
      if (entry.grantId) {
        await kv.put(`grant:${entry.grantId}`, JSON.stringify({
          grantId: entry.grantId,
          role: entry.role,
          payload: entry.payload,
          ownerEmail: body.fromEmail || body.ownerEmail || null,
          updatedAt: entry.createdAt,
        }));
      }
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

    if (request.method === 'POST' && url.pathname === '/grant') {
      const body = await request.json();
      const grantId = body.grantId;
      if (!grantId || !body.payload) return json({ error: 'Invalid grant' }, 400);
      await kv.put(`grant:${grantId}`, JSON.stringify({
        grantId,
        role: body.role || 'viewer',
        payload: body.payload,
        ownerEmail: body.ownerEmail || null,
        updatedAt: body.updatedAt || new Date().toISOString(),
      }));
      return json({ ok: true });
    }

    if (request.method === 'GET' && url.pathname === '/grant') {
      const grantId = url.searchParams.get('id');
      if (!grantId) return json({ error: 'Missing id' }, 400);
      const raw = await kv.get(`grant:${grantId}`);
      if (!raw) return json({ error: 'Not found' }, 404);
      return new Response(raw, { headers: { ...CORS, 'Content-Type': 'application/json' } });
    }

    return json({ error: 'Not found' }, 404);
  },
};
