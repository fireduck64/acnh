package(default_visibility = ["//visibility:public"])

java_library(
  name = "acnhlib",
  srcs = glob(["src/**/*.java", "src/*.java"]),
  deps = [
    "@duckutil//:duckutil_lib",
    "@maven//:com_google_guava_guava",
  ],
)

java_binary(
  name = "FlowerSim",
  main_class = "fireduck.acnh.FlowerSim",
  runtime_deps = [
    ":acnhlib",
  ],
)

