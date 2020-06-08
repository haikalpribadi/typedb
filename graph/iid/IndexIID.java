/*
 * Copyright (C) 2020 Grakn Labs
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package hypergraph.graph.iid;

import hypergraph.graph.util.Schema;

import javax.annotation.Nullable;
import java.time.LocalDateTime;

import static hypergraph.common.collection.Bytes.DOUBLE_SIZE;
import static hypergraph.common.collection.Bytes.LONG_SIZE;
import static hypergraph.common.collection.Bytes.booleanToByte;
import static hypergraph.common.collection.Bytes.byteToBoolean;
import static hypergraph.common.collection.Bytes.bytesToDateTime;
import static hypergraph.common.collection.Bytes.bytesToDouble;
import static hypergraph.common.collection.Bytes.bytesToLong;
import static hypergraph.common.collection.Bytes.bytesToString;
import static hypergraph.common.collection.Bytes.dateTimeToBytes;
import static hypergraph.common.collection.Bytes.doubleToBytes;
import static hypergraph.common.collection.Bytes.join;
import static hypergraph.common.collection.Bytes.longToBytes;
import static hypergraph.common.collection.Bytes.stringToBytes;
import static hypergraph.graph.util.Schema.STRING_ENCODING;
import static hypergraph.graph.util.Schema.TIME_ZONE_ID;
import static java.util.Arrays.copyOfRange;

public abstract class IndexIID extends IID {

    IndexIID(byte[] bytes) {
        super(bytes);
    }

    public static class Type extends IndexIID {

        Type(byte[] bytes) {
            super(bytes);
        }

        /**
         * Returns the index address of given {@code TypeVertex}
         *
         * @param label of the {@code TypeVertex}
         * @param scope of the {@code TypeVertex}, which could be null
         * @return a byte array representing the index address of a {@code TypeVertex}
         */
        public static Type of(String label, @Nullable String scope) {
            return new Type(join(Schema.Index.TYPE.prefix().bytes(), Schema.Vertex.Type.scopedLabel(label, scope).getBytes(STRING_ENCODING)));
        }

        @Override
        public String toString() {
            return "[" + PrefixIID.LENGTH + ": " + Schema.Index.TYPE.toString() + "]" +
                    "[" + (bytes.length - PrefixIID.LENGTH) + ": " + bytesToString(copyOfRange(bytes, PrefixIID.LENGTH, bytes.length), STRING_ENCODING) + "]";
        }
    }

    public static class Attribute extends IndexIID {

        static final int VALUE_INDEX = PrefixIID.LENGTH + VertexIID.Attribute.VALUE_TYPE_LENGTH;

        Attribute(byte[] bytes) {
            super(bytes);
        }

        private static Attribute newAttributeIndex(byte[] valueType, byte[] value, byte[] typeIID) {
            return new Attribute(join(Schema.Index.ATTRIBUTE.prefix().bytes(), valueType, value, typeIID));
        }

        public static Attribute of(boolean value, VertexIID.Type typeIID) {
            return newAttributeIndex(Schema.ValueType.BOOLEAN.bytes(), new byte[]{booleanToByte(value)}, typeIID.bytes);
        }

        public static Attribute of(long value, VertexIID.Type typeIID) {
            return newAttributeIndex(Schema.ValueType.LONG.bytes(), longToBytes(value), typeIID.bytes);
        }

        public static Attribute of(double value, VertexIID.Type typeIID) {
            return newAttributeIndex(Schema.ValueType.DOUBLE.bytes(), doubleToBytes(value), typeIID.bytes);
        }

        public static Attribute of(String value, VertexIID.Type typeIID) {
            return newAttributeIndex(Schema.ValueType.STRING.bytes(), stringToBytes(value, STRING_ENCODING), typeIID.bytes);
        }

        public static Attribute of(LocalDateTime value, VertexIID.Type typeIID) {
            return newAttributeIndex(Schema.ValueType.DATETIME.bytes(), dateTimeToBytes(value, TIME_ZONE_ID), typeIID.bytes);
        }

        @Override
        public String toString() {
            Schema.ValueType valueType = Schema.ValueType.of(bytes[PrefixIID.LENGTH]);
            String value;
            assert valueType != null;
            switch (valueType) {
                case BOOLEAN:
                    value = byteToBoolean(bytes[VALUE_INDEX]).toString();
                    break;
                case LONG:
                    value = "" + bytesToLong(copyOfRange(bytes, VALUE_INDEX, VALUE_INDEX + LONG_SIZE));
                    break;
                case DOUBLE:
                    value = "" + bytesToDouble(copyOfRange(bytes, VALUE_INDEX, VALUE_INDEX + DOUBLE_SIZE));
                    break;
                case STRING:
                    value = bytesToString(copyOfRange(bytes, VALUE_INDEX, bytes.length - VertexIID.Type.LENGTH), STRING_ENCODING);
                    break;
                case DATETIME:
                    value = bytesToDateTime(copyOfRange(bytes, VALUE_INDEX, bytes.length - VertexIID.Type.LENGTH), TIME_ZONE_ID).toString();
                    break;
                default:
                    value = "";
                    break;
            }

            return "[" + PrefixIID.LENGTH + ": " + Schema.Index.ATTRIBUTE.toString() + "]" +
                    "[" + VertexIID.Attribute.VALUE_TYPE_LENGTH + ": " + valueType.toString() + "]" +
                    "[" + (bytes.length - (PrefixIID.LENGTH + VertexIID.Attribute.VALUE_TYPE_LENGTH + VertexIID.Type.LENGTH)) + ": " + value + "]" +
                    "[" + VertexIID.Type.LENGTH + ": " + VertexIID.Type.of(copyOfRange(bytes, bytes.length - VertexIID.Type.LENGTH, bytes.length)).toString() + "]";
        }
    }
}
