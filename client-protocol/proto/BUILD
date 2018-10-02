#
# GRAKN.AI - THE KNOWLEDGE GRAPH
# Copyright (C) 2018 Grakn Labs Ltd
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as
# published by the Free Software Foundation, either version 3 of the
# License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with this program.  If not, see <https://www.gnu.org/licenses/>.
#

proto_library(
    name = "client-proto",
    srcs = glob(["*.proto"]),
    proto_source_root = "client-protocol/proto",
)

java_proto_library(
    name = "client-java-proto",
    deps = [":client-proto"],
)

load("@io_grpc_grpc_java//:java_grpc_library.bzl", "java_grpc_library")
java_grpc_library(
    name = "client-java-grpc",
    srcs = [":client-proto"],
    deps = [":client-java-proto"],
)