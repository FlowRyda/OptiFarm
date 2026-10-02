/**
 * formats a date or iso string for <input type="date">
 * Output: "YYYY-MM-DD"
 */
export function toHtmlDate(value: string | Date | null | undefined): string {
  if (!value) return '';
  const date = typeof value === 'string' ? new Date(value) : value;
  if (isNaN(date.getTime())) return '';

  const pad = (n: number) => n < 10 ? '0' + n : n;
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
}

/**
 * formats a date or iso string for use in <input type="datetime-local">
 * Output: "yyyy-MM-ddTHH:mm"
 */
export function toHtmlDateTime(value: string | Date | null | undefined): string {
  if (!value) return '';
  const date = typeof value === 'string' ? new Date(value) : value;
  if (isNaN(date.getTime())) return '';

  const pad = (n: number) => n < 10 ? '0' + n : n;
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
}

/**
 * Formats a date-time string for backend consumption.
 * @param value
 */
export function toBackendDateTime(value: string | null | undefined): string | null {
  if (!value) return null;
  // seconds given (length 19: YYYY-MM-DDTHH:mm:ss) => do nothing
  if (value.length === 19) return value;
  // Seconds missing (length 16) => append :00
  if (value.length === 16) return `${value}:00`;
  return value;
}

/**
 * Combines a date (YYYY-MM-DD) with a fixed time
 */
export function createFixedDateTime(dateOnlyStr: string | null, hour: number, minute: number): string | null {
  if (!dateOnlyStr) return null;
  const datePart = dateOnlyStr.substring(0, 10);

  const pad = (n: number) => n < 10 ? '0' + n : n;
  return `${datePart}T${pad(hour)}:${pad(minute)}:00`;
}

/**
 * Formats a date range. If both dates are on the same day, the date is only shown once.
 * Example outputs:
 * - "12.01.2026, 06:00 – 07:00"
 * - "12.01.2026, 06:00 – 13.01.2026, 07:00"
 */
export function formatSmartDateRange(startVal: string | Date | undefined, endVal: string | Date | undefined): string {
  if (!startVal || !endVal) return '–';

  const start = typeof startVal === 'string' ? new Date(startVal) : startVal;
  const end = typeof endVal === 'string' ? new Date(endVal) : endVal;

  if (isNaN(start.getTime()) || isNaN(end.getTime())) return '–';

  const isSameDay =
    start.getDate() === end.getDate() &&
    start.getMonth() === end.getMonth() &&
    start.getFullYear() === end.getFullYear();

  const dateOpt: Intl.DateTimeFormatOptions = {day: '2-digit', month: '2-digit', year: 'numeric'};
  const timeOpt: Intl.DateTimeFormatOptions = {hour: '2-digit', minute: '2-digit'};

  // Format: dd.MM.yyyy
  const startDateStr = start.toLocaleDateString('de-DE', dateOpt);
  const endDateStr = end.toLocaleDateString('de-DE', dateOpt);

  // Format: HH:mm
  const startTimeStr = start.toLocaleTimeString('de-DE', timeOpt);
  const endTimeStr = end.toLocaleTimeString('de-DE', timeOpt);

  if (isSameDay) {
    // "12.01.2026, 06:00 – 07:00"
    return `${startDateStr}, ${startTimeStr} – ${endTimeStr}`;
  } else {
    // "12.01.2026, 06:00 – 13.01.2026, 07:00"
    return `${startDateStr}, ${startTimeStr} – ${endDateStr}, ${endTimeStr}`;
  }
}


export function addWorkHours(startDate: Date, hoursToAdd: number, startHourOfDay: number, endHourOfDay: number): Date {
  let current = new Date(startDate);
  let minutesRemaining = hoursToAdd * 60;

  if (current.getHours() < startHourOfDay) {
    current.setHours(startHourOfDay, 0, 0, 0);
  } else if (current.getHours() >= endHourOfDay) {
    current.setDate(current.getDate() + 1);
    current.setHours(startHourOfDay, 0, 0, 0);
  }

  while (minutesRemaining > 0) {
    const currentHour = current.getHours();
    const currentMinute = current.getMinutes();

    const minutesUntilEndOfDay = ((endHourOfDay - currentHour) * 60) - currentMinute;

    if (minutesRemaining <= minutesUntilEndOfDay) {
      current.setMinutes(currentMinute + minutesRemaining);
      minutesRemaining = 0;
    } else {
      // Longer than remaining workday
      minutesRemaining -= minutesUntilEndOfDay;

      current.setDate(current.getDate() + 1);
      current.setHours(startHourOfDay, 0, 0);
    }

  }

  return current;
}

export function calculateTaskEndTime(
  startIso: string | null | undefined,
  durationHours: number | undefined,
  startHour: number,
  endHour: number
): string | null {
  if (!startIso || durationHours == null || durationHours === 0) return null;

  const startDate = new Date(startIso);

  if (isNaN(startDate.getTime()) || isNaN(durationHours)) return null;

  const endDate = addWorkHours(startDate, durationHours, startHour, endHour);

  return toHtmlDateTime(endDate);
}

export function roundUpToMinuteStepForDate(date: Date, stepMinutes: number): Date {
  const d = new Date(date);
  const ms = d.getTime();
  const stepMs = stepMinutes * 60 * 1000;
  const rounded = Math.ceil(ms / stepMs) * stepMs;
  return new Date(rounded);
}

/** workTimeEstimate is in hours (e.g. 1.1h). Round UP to next 15 minutes => 0.25h steps. */
export function roundUpHoursToQuarter(hours: number): number {
  if (!Number.isFinite(hours) || hours < 0) return hours;
  const minutes = hours * 60;
  const roundedMinutes = Math.ceil(minutes / 15) * 15;
  return roundedMinutes / 60;
}
