{
  description = "Java 25 and Maven development environment";

  inputs = {
    nixpkgs.url = "github:nixos/nixpkgs/nixos-unstable";
  };

  outputs =
    { self, nixpkgs }:
    let
      supportedSystems = [
        "x86_64-linux"
        "aarch64-linux"
        "x86_64-darwin"
        "aarch64-darwin"
      ];
      forEachSystem =
        f: nixpkgs.lib.genAttrs supportedSystems (system: f (import nixpkgs { inherit system; }));
    in
    {
      devShells = forEachSystem (pkgs: {
        default =
          let
            # This packages your command as a universal executable binary script
            run-lanacchain = pkgs.writeShellScriptBin "run-lanacchain" ''
              exec mvn compile exec:java -Dexec.mainClass="com.kameni.lanacchain.LanacChain" "$@"
            '';
          in
          pkgs.mkShell {
            buildInputs = [
              pkgs.temurin-bin-25
              pkgs.maven
              run-lanacchain
            ];

            shellHook = ''
              export JAVA_HOME="${pkgs.temurin-bin-25}"

              echo "LanacChain environment active."
              echo "Java version: $(java -version 2>&1 | head -n 1)"
              echo "Maven version: $(mvn -version | head -n 1)"
              echo "Type 'run-lanacchain' to compile and run."
            '';
          };
      });
    };
}
