load(
    "@com_github_google_bazel_common//tools/maven:pom_file.bzl",
    pom_file_exports = "exports",
)

def warn(msg):
    print('{red}{msg}{nc}'.format(red='\033[0;31m', msg=msg, nc='\033[0m'))

def _maven_nexus_deploy_impl(ctx):
    preprocessed_template = ctx.actions.declare_file("_pom.xml")
    preprocessed_script = ctx.actions.declare_file("_deploy.sh")
    
    mock_ctx = struct(
        attr = struct(
            targets = ctx.attr.targets,
            preferred_group_ids = [],
            excluded_artifacts = [],
            substitutions = {
                "{parent_group_id}": ctx.attr.parent_group_id,
                "{parent_artifact_id}": ctx.attr.parent_artifact_id,
                "{artifact_id}": ctx.attr.artifact_id
            }
        ),
        var = {
            "pom_version": "{pom_version}"
        },
        actions = ctx.actions,
        file = struct(
            template_file = ctx.file._pom_xml_template
        ),
        outputs = struct(
            pom_file = preprocessed_template
        )
    )

    pom_file_exports._pom_file(mock_ctx)

    if (len(ctx.attr.targets) != 1):
        fail("should specify single jar to deploy")

    if (len(ctx.attr.targets[0].java.outputs.jars) != 1):
        fail("should specify rule that produces a single jar")

    jar = ctx.attr.targets[0].java.outputs.jars[0].class_jar
    parent_coords = "/".join([
        ctx.attr.parent_group_id.replace('.', '/'),
        ctx.attr.parent_artifact_id
    ])

    if not ctx.var.get("MAVEN_USERNAME"):
        warn("should specify username via --define=MAVEN_USERNAME= argument to bazel run")

    if not ctx.var.get("MAVEN_PASSWORD"):
        warn("should specify password via --define=MAVEN_PASSWORD= argument to bazel run")

    if not ctx.var.get("MAVEN_URL"):
        warn("should specify Maven url via --define=MAVEN_URL= argument to bazel run")

    ctx.actions.run_shell(
        inputs = [preprocessed_template, ctx.file.version_file],
        outputs = [ctx.outputs.pom_file],
        command = "VERSION=`cat %s` && sed -e s/{pom_version}/$VERSION/g %s > %s" % (
            ctx.file.version_file.path, preprocessed_template.path, ctx.outputs.pom_file.path)
    )

    ctx.actions.run_shell(
        inputs = [ctx.file._deployment_script_template, ctx.file.version_file],
        outputs = [preprocessed_script],
        command = "VERSION=`cat %s` && sed -e s/{pom_version}/$VERSION/g %s > %s" % (
            ctx.file.version_file.path, ctx.file._deployment_script_template.path, preprocessed_script.path)
    )

    ctx.actions.expand_template(
        template = preprocessed_script,
        output = ctx.outputs.deployment_script,
        substitutions = {
            "$ARTIFACT": ctx.attr.artifact_id,
            "$MAVEN_PASSWORD": ctx.var.get("MAVEN_PASSWORD", "$MAVEN_PASSWORD"),
            "$MAVEN_URL": ctx.var.get("MAVEN_URL", "$MAVEN_URL"),
            "$MAVEN_USERNAME": ctx.var.get("MAVEN_USERNAME", "$MAVEN_USERNAME"),
            "$PARENT": parent_coords,
        },
        is_executable = True
    )

    return DefaultInfo(executable = ctx.outputs.deployment_script,
        runfiles = ctx.runfiles(
            files=[jar, ctx.outputs.pom_file],
            symlinks={
                "lib.jar": jar,
                "pom.xml": ctx.outputs.pom_file
            }))

maven_nexus_deploy = rule(
    attrs = {
        "targets": attr.label_list(
            mandatory = True,
            aspects = [pom_file_exports._collect_maven_info],
        ),
        "parent_group_id": attr.string(mandatory = True),
        "parent_artifact_id": attr.string(mandatory = True),
        "artifact_id": attr.string(mandatory = True),
        "version_file": attr.label(
            allow_single_file = True,
            mandatory = True,
        ),
        "_pom_xml_template": attr.label(
            allow_single_file = True,
            default = "//dependencies/maven/deployment:pom_template.xml",
        ),
        "_deployment_script_template": attr.label(
            allow_single_file = True,
            default = "//dependencies/maven/deployment:deploy.sh",
        ),
    },
    executable = True,
    outputs = {
        "pom_file": "%{name}.xml",
        "deployment_script": "%{name}.sh",
    },
    implementation = _maven_nexus_deploy_impl,
)
