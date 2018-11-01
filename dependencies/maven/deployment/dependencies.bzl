def maven_deployment_dependencies():
    native.git_repository(
        name="com_github_google_bazel_common",
        remote = "https://github.com/graknlabs/bazel-common.git",
        commit = "39d112df25022595692e12739145de87d6bd61e5"
    )
