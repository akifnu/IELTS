# AGENTS.md

## Cursor Cloud specific instructions

### Product overview

Single-service **IELTS Writing Task 2 checker** — a Streamlit web app (`IELTS_app.py`) that generates prompts, validates essay length (≥250 words), and calls OpenAI (`gpt-4o-mini`) for feedback, scoring, and corrections.

### Running the app

1. Ensure `~/.local/bin` is on `PATH` (pip `--user` installs `streamlit` there).
2. Configure OpenAI credentials in `.streamlit/secrets.toml` (see below).
3. Start the server:

```bash
export PATH="$HOME/.local/bin:$PATH"
streamlit run IELTS_app.py --server.port 8501 --server.headless true --server.enableCORS false --server.enableXsrfProtection false
```

4. Open http://localhost:8501

Use a dedicated tmux session (e.g. `streamlit-ielts`) for long-running dev servers.

### Secrets (required for AI features)

The app reads `st.secrets["OPENAI_API_KEY"]["OPENAI_API_KEY"]` at import time. Create `.streamlit/secrets.toml`:

```toml
[OPENAI_API_KEY]
OPENAI_API_KEY = "sk-..."
```

If the `OPENAI_API_KEY` environment variable is set, the VM update script writes this file automatically. Without a valid key, the UI and word-count validation still work, but prompt generation and essay analysis return `openai.AuthenticationError`.

### Lint / test / build

- No automated test suite or linter is configured in this repo.
- Syntax check: `python3 -m py_compile IELTS_app.py`
- Dependencies: `pip3 install --user -r requirements.txt`

### Optional / not E2E-ready

- `Untitled-6.ipynb` — experimental notebook (Groq + FAISS); missing binary assets and extra deps not in `requirements.txt`.
- `.devcontainer/devcontainer.json` — Codespaces convenience; mirrors the same `pip install` + `streamlit run` flow.
