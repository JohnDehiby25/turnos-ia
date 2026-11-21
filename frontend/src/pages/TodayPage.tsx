import { useState } from 'react'

const todayFormatter = new Intl.DateTimeFormat('es-CO', {
  weekday: 'long',
  day: 'numeric',
  month: 'long',
  timeZone: 'America/Bogota',
})

export function TodayPage() {
  const [today] = useState(() => todayFormatter.format(new Date()))

  return (
    <main className="mx-auto flex w-full max-w-3xl flex-col gap-4 px-5 py-8">
      <p className="text-lg capitalize text-neutral-700">{today}</p>
      <h1 className="font-display text-4xl font-bold text-ink">Hoy</h1>
      <p className="border-l-4 border-cobalt bg-mist px-4 py-4 text-lg text-ink">
        Aquí aparecerán los turnos de hoy, en orden de hora. Esta pantalla llega en la siguiente tarea.
      </p>
    </main>
  )
}
