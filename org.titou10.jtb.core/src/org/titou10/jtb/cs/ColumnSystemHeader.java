/*
 * Copyright (C) 2015-2017 Denis Forveille titou10.titou10@gmail.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.titou10.jtb.cs;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.jms.JMSException;
import javax.jms.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.jms.model.JTBMessageType;
import org.titou10.jtb.jms.util.JTBDeliveryMode;

/**
 * 
 * JMS Artefacts (JMS Header + other) considered as "System" data that can be displayed as a column in the Message Browser
 * 
 * @author Denis Forveille
 *
 */
public enum ColumnSystemHeader {

                                JMS_DESTINATION("JMSDestination", "Destination", 200),
                                JMS_DELIVERY_MODE("JMSDeliveryMode", "Delivery Mode", 100),
                                JMS_EXPIRATION("JMSExpiration", "Expiration", 140),
                                JMS_DELIVERY_TIME("DeliveryTime", "Delivery Time", 140),
                                JMS_PRIORITY("JMSPriority", "Priority", 60),
                                JMS_MESSAGE_ID("JMSMessageID", "ID", 200),
                                JMS_TIMESTAMP("JMSTimestamp", "JMS Timestamp", 140),
                                JMS_CORRELATION_ID("JMSCorrelationID", "JMS Correlation ID", 150),
                                JMS_REPLY_TO("JMSReplyTo", "Reply To", 200),
                                JMS_TYPE("JMSType", "JMS Type", 100),
                                JMS_REDELIVERED("JMSRedelivered", "Redelivered", 60),

                                MESSAGE_TYPE("Message Class", "Type", 60);

   private static final Logger                           log     = LoggerFactory.getLogger(ColumnSystemHeader.class);
   private static final SimpleDateFormat                 SDF     = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

   private String                                        headerName;
   private String                                        displayName;
   private int                                           displayWidth;

   // Map for performance: ColumnSystemHeader.getHeaderName().hashCode() -> ColumnSystemHeader
   private static final Map<Integer, ColumnSystemHeader> MAP_CSH = new HashMap<>();

   // -----------
   // Constructor
   // -----------

   private ColumnSystemHeader(String headerName, String displayName, int displayWidth) {
      this.headerName = headerName;
      this.displayName = displayName;
      this.displayWidth = displayWidth;
   }

   static {
      MAP_CSH.putAll(Arrays.stream(values()).collect(Collectors.toMap(csh -> csh.getHeaderName().hashCode(), csh -> csh)));
   }

   // ----------------
   // Helpers
   // ----------------
   public static ColumnSystemHeader fromHeaderName(String columnSystemHeaderName) {
      return MAP_CSH.get(columnSystemHeaderName.hashCode());
   }

   // public static String getColumnSystemValue(Message m, String columnSystemHeaderName) {
   // ColumnSystemHeader columnSystemHeader = fromHeaderName(columnSystemHeaderName);
   // return columnSystemHeader == null ? "" : columnSystemHeader.getColumnSystemValue(m);
   // }

   public String getColumnSystemValue(Message m) {

      // DF: could probably better be implemented via a java 8 Function<>

      try {

         switch (this) {
            case JMS_CORRELATION_ID:
               return m.getJMSCorrelationID() == null ? "" : m.getJMSCorrelationID();

            case JMS_DELIVERY_MODE:
               StringBuilder deliveryMode = new StringBuilder(32);
               deliveryMode.append(JTBDeliveryMode.fromValue(m.getJMSDeliveryMode()).name());
               deliveryMode.append(" (");
               deliveryMode.append(m.getJMSDeliveryMode());
               deliveryMode.append(")");
               return deliveryMode.toString();

            case JMS_DELIVERY_TIME:
               return m.getJMSDeliveryTime() == 0 ? "" : SDF.format(new Date(m.getJMSDeliveryTime()));

            case JMS_DESTINATION:
               return m.getJMSDestination() == null ? "" : m.getJMSDestination().toString();

            case JMS_EXPIRATION:
               return m.getJMSExpiration() == 0 ? "" : SDF.format(new Date(m.getJMSExpiration()));

            case JMS_MESSAGE_ID:
               return m.getJMSMessageID();

            case JMS_PRIORITY:
               return String.valueOf(m.getJMSPriority());

            case JMS_REDELIVERED:
               return String.valueOf(m.getJMSRedelivered());

            case JMS_REPLY_TO:
               return m.getJMSReplyTo() == null ? "" : m.getJMSReplyTo().toString();

            case JMS_TIMESTAMP:
               return m.getJMSTimestamp() == 0 ? "" : SDF.format(new Date(m.getJMSTimestamp()));

            case JMS_TYPE:
               return m.getJMSType();

            case MESSAGE_TYPE:
               return JTBMessageType.fromJMSMessage(m).getDescription();
         }
      } catch (JMSException e) {
         log.warn("JMSException occured when reading JMS header '{}' : {}", this.getHeaderName(), e.getMessage());
      }
      return "";

   }

   // ----------------
   // Standard Getters
   // ----------------
   public String getHeaderName() {
      return headerName;
   }

   public String getDisplayName() {
      return displayName;
   }

   public int getDisplayWidth() {
      return displayWidth;
   }

}