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

workspace(name = "grakn_core")


###################
# Load Buil Tools #
###################

# Load additional build tools, such bazel-deps and unused-deps
load("//dependencies/tools:dependencies.bzl", "tools_dependencies")
tools_dependencies()


#####################################
# Load Compiler Dependencies: ANTLR #
#####################################

# Load compiler tool: ANTLR
load("//dependencies/compilers:dependencies.bzl", "antlr_dependencies")
antlr_dependencies()

# Load dependencies for ANTLR rules
load("@rules_antlr//antlr:deps.bzl", "antlr_dependencies")
antlr_dependencies()


####################################
# Load Compiler Dependencies: GRPC #
####################################

# Load Protobuf dependencies
load("//dependencies/compilers:dependencies.bzl", "grpc_dependencies")
grpc_dependencies()

load("@io_grpc_grpc_java//:repositories.bzl", "grpc_java_repositories")
grpc_java_repositories(
    omit_com_google_auth_google_auth_library_credentials = True,
    omit_com_google_protobuf_nano_protobuf_javanano = True,
    omit_com_google_truth_truth = True,
    omit_com_squareup_okhttp = True,
    omit_com_squareup_okio = True,
    omit_io_netty_buffer = True,
    omit_io_netty_codec = True,
    omit_io_netty_codec_http = True,
    omit_io_netty_codec_http2 = True,
    omit_io_netty_codec_socks = True,
    omit_io_netty_common = True,
    omit_io_netty_handler = True,
    omit_io_netty_handler_proxy = True,
    omit_io_netty_resolver = True,
    omit_io_netty_tcnative_boringssl_static = True,
    omit_io_netty_transport = True,
    omit_io_opencensus_api = True,
    omit_io_opencensus_grpc_metrics = True,
    omit_junit_junit = True,
    omit_org_apache_commons_lang3 = True,
)


########################################
# Load Runtime Dependencies from Maven #
########################################

load("//dependencies/maven:dependencies.bzl", "maven_dependencies")
maven_dependencies()