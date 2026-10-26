import { useEffect, useRef, useState } from 'react';
import meetingsService from '../services/meetingsService';

/**
 * Manages heartbeat for an active meeting.
 *
 * Sends:
 *   - join   → once on mount
 *   - heartbeat → every 30 seconds
 *   - leave  → on unmount and beforeunload
 *
 * Returns elapsed seconds since joining.
 */
export const useMeetingHeartbeat = (meetingId, isActive) => {
  const [elapsedSeconds, setElapsedSeconds] = useState(0);
  const [connected, setConnected] = useState(false);
  const startTimeRef = useRef(null);
  const heartbeatRef = useRef(null);
  const timerRef = useRef(null);

  useEffect(() => {
    if (!isActive || !meetingId) return;

    let cancelled = false;

    // ---- 1. Send join ----
    const join = async () => {
      try {
        await meetingsService.join(meetingId);
        if (cancelled) return;
        startTimeRef.current = Date.now();
        setConnected(true);
      } catch (err) {
        console.error('Join failed:', err);
      }
    };
    join();

    // ---- 2. Heartbeat every 30 seconds ----
    heartbeatRef.current = setInterval(async () => {
      try {
        await meetingsService.heartbeat(meetingId);
        setConnected(true);
      } catch (err) {
        setConnected(false);
      }
    }, 30000);

    // ---- 3. Live timer (every second) ----
    timerRef.current = setInterval(() => {
      if (startTimeRef.current) {
        setElapsedSeconds(Math.floor((Date.now() - startTimeRef.current) / 1000));
      }
    }, 1000);

    // ---- 4. Send leave on browser close ----
    const handleUnload = () => {
      const token = localStorage.getItem('kikoba_token');
      const url = `/api/meetings/${meetingId}/leave`;
      if (navigator.sendBeacon) {
        // sendBeacon can't set headers — use Blob with auth in query
        // Backend accepts auth from JWT filter — but beacon won't send it
        // Fallback: use fetch with keepalive
        fetch(url, {
          method: 'POST',
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json',
          },
          keepalive: true,
        }).catch(() => {});
      }
    };
    window.addEventListener('beforeunload', handleUnload);

    // ---- Cleanup ----
    return () => {
      cancelled = true;
      if (heartbeatRef.current) clearInterval(heartbeatRef.current);
      if (timerRef.current) clearInterval(timerRef.current);
      window.removeEventListener('beforeunload', handleUnload);

      // Send leave on component unmount
      meetingsService.leave(meetingId).catch(() => {});
    };
  }, [meetingId, isActive]);

  return { elapsedSeconds, connected };
};

export default useMeetingHeartbeat;