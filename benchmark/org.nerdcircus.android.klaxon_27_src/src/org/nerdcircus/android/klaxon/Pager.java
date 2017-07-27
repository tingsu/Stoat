/* 
 * Copyright 2008 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.nerdcircus.android.klaxon;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import android.R.drawable;

public final class Pager
{
    public static final int[] statusIcons = {
        android.R.drawable.presence_offline,
        android.R.drawable.presence_online,
        android.R.drawable.presence_busy
        };

    public static final String PREFS_FILE = "klaxon_prefs";

    public static final String REPLY_ACTION = "org.nerdcircus.android.klaxon.REPLY";
    public static final String ACK_ACTION = "org.nerdcircus.android.klaxon.ACK";
    public static final String NACK_ACTION = "org.nerdcircus.android.klaxon.NACK";
    public static final String PAGE_RECEIVED = "org.nerdcircus.android.klaxon.PAGE_RECEIVED";
    public static final String ANNOY_ACTION = "org.nerdcircus.android.klaxon.ANNOY";
    public static final String SILENCE_ACTION = "org.nerdcircus.android.klaxon.PAGES_VIEWED";

    public static final String EXTRA_NEW_ACK_STATUS = "org.nerdcircus.android.klaxon.NEW_ACK_STATUS";

    public static final int STATUS_NONE = 0;
    public static final int STATUS_ACK = 1;
    public static final int STATUS_NACK = 2;

    /**
     * get the icon to display for the given ack_status value.
     */
    public static int getStatusResId(int status){
        return statusIcons[status];
    }

    /**
     * Pages database
     */
    public static final class Pages implements BaseColumns
    {
        public static final String TABLE_NAME = "pages";
        public static final Cursor query(ContentResolver cr, String[] projection)
        {
            return cr.query(CONTENT_URI, 
                            projection,
                            null,
                            null,
                            DEFAULT_SORT_ORDER);
        }

        public static final Cursor query(ContentResolver cr, String[] projection,
                                       String where, String orderBy)
        {
            return cr.query(CONTENT_URI, projection, where, null,
                                         orderBy == null ? DEFAULT_SORT_ORDER : orderBy);
        }

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI =
            Uri.parse("content://org.nerdcircus.android.klaxon/pages");

        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = "created DESC";

        /**
         * The subject of the page
         * <P>Type: TEXT</P>
         */
        public static final String SUBJECT = "subject";

        /**
         * The text of the page
         * <P>Type: TEXT</P>
         */
        public static final String BODY = "body";

        /**
         * The timestamp for when the page was created
         * <P>Type: INTEGER (long)</P>
         */
        public static final String CREATED_DATE = "created";

        /**
         * The service center address from which the page was received.
         * <P>Type: TEXT</P>
         */
        public static final String SERVICE_CENTER = "sc_addr";

        /**
         * The originating address
         * <P>Type: TEXT</P>
         */
        public static final String SENDER = "sender_addr";

        /**
         * Acknowledgement status
         * <P>Type: INTEGER</P>
         */
        public static final String ACK_STATUS = "ack_status";

        /**
         * Email From address
         * <P>Type: TEXT</P>
         */
        public static final String FROM_ADDR = "email_from_addr";

        /**
         * String to identify the transport over which this page was received.
         * <P>Type: TEXT</P>
         */
        public static final String TRANSPORT = "transport";
    }

    /**
     * Replies table.
     * stores our snappy comebacks.
     */
    public static final class Replies implements BaseColumns
    {
        public static final String TABLE_NAME = "replies";
        public static final Cursor query(ContentResolver cr, String[] projection)
        {
            return cr.query(CONTENT_URI, 
                            projection,
                            null,
                            null,
                            DEFAULT_SORT_ORDER);
        }

        public static final Cursor query(ContentResolver cr, String[] projection,
                                       String where, String orderBy)
        {
            return cr.query(CONTENT_URI, projection, where, null,
                                         orderBy == null ? DEFAULT_SORT_ORDER : orderBy);
        }

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI =
            Uri.parse("content://org.nerdcircus.android.klaxon/reply");

        /**
         * Short name of our reply.
         */
        public static final String NAME = "name";

        /**
         * content of the reply.
         */
        public static final String BODY = "body";
        /**
         * new "ack status". integer. 
         */
        public static final String ACK_STATUS = "ack_status";
        /**
         * whether this item should be shown in the menu.
         */
        public static final String SHOW_IN_MENU = "show_in_menu";

        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = "name ASC";
    }
}

