/*
 * Copyright (c) 2014
 *
 * ApkTrack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ApkTrack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ApkTrack.  If not, see <http://www.gnu.org/licenses/>.
 */

package fr.kwiatkowski.ApkTrack;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.SystemClock;
import com.commonsware.cwac.wakeful.WakefulIntentService;

public class PollReciever implements WakefulIntentService.AlarmListener
{
    public static final long DELAY = AlarmManager.INTERVAL_DAY;

    public void scheduleAlarms(AlarmManager mgr, PendingIntent pi, Context ctxt)
    {
        mgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,SystemClock.elapsedRealtime() + 1000, DELAY, pi);
    }

    public void sendWakefulWork(Context ctxt) {
        WakefulIntentService.sendWakefulWork(ctxt, ScheduledVersionCheckService.class);
    }

    public long getMaxAge() {
        return(AlarmManager.INTERVAL_DAY * 2);
    }
}