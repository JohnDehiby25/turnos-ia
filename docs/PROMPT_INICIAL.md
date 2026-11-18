# Cómo empezar (paso a paso)

## 1. Preparar el repo
```bash
cd turnos-ia
cp .env.example .env        # edita DB_PASSWORD y JWT_SECRET (el .env NO se sube a GitHub)
git init -b main
git add .
git commit -m "chore: base del proyecto (docs, skills y config de OpenCode)"
# crea un repo PRIVADO en GitHub llamado turnos-ia y luego:
git remote add origin https://github.com/<tu-usuario>/turnos-ia.git
git push -u origin main
```

## 2. Abrir OpenCode
```bash
opencode
```
Conecta tu modelo si no lo has hecho (`/connect`). Escribe `/estado`.

## 3. Primer prompt
```
Lee AGENTS.md, docs/PROGRESO.md y docs/ROADMAP.md. Estamos en la etapa E0.
Sigue el protocolo por etapas: resúmeme el estado y pregúntame con qué tarea seguimos.
No escribas código todavía.
```

## 4. Cuando te responda
Responde algo como: "Seguimos con E0-T1". Debe darte un plan de 3–5 viñetas antes de tocar archivos. Revísalo y di "ok".

## Frases útiles durante el trabajo
- Si se desvía: `Detente. Vuelve a la tarea actual del roadmap y no avances de etapa.`
- Si no entiendes algo: `Explícame el patrón o la decisión que usaste, con un ejemplo corto.`
- Para ver avance: `/avance`
- Para cerrar: `Cerramos por hoy: deja el punto de control en docs/PROGRESO.md.`
- Para la siguiente sesión: `/estado`

## Orden recomendado
E0 → E1 (turnos) → E2 (panel) → E3 (IA) → E4 (nube) → E5 (WhatsApp) → E6 (mobile).
Precios y resumen de dinero entran en E1-T8/T9 y E2-T8/T9, después del núcleo de turnos.
