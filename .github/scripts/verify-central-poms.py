#!/usr/bin/env python3
"""Verify the release-candidate Maven POMs without publishing anything."""
from pathlib import Path
import xml.etree.ElementTree as ET

EXPECTED = {"bedrock-codec": "1.26.60-R1", "bedrock-connection": "1.26.60-R1"}
GROUP = "io.github.ryzemcbe"
for module, version in EXPECTED.items():
    pom = Path(module) / "build" / "publications" / "maven" / "pom-default.xml"
    assert pom.is_file(), f"Missing generated publication POM: {pom}"
    root = ET.parse(pom).getroot()
    def val(name):
        node = root.find("{*}" + name)
        return node.text.strip() if node is not None and node.text else None
    assert val("groupId") == GROUP, (pom, "groupId", val("groupId"))
    assert val("artifactId") == module, (pom, "artifactId", val("artifactId"))
    assert val("version") == version, (pom, "version", val("version"))
    assert val("name"), f"Missing POM name for {module}"
    assert root.find("{*}licenses/{*}license") is not None, f"Missing license in {module}"
    assert root.find("{*}developers/{*}developer") is not None, f"Missing developers in {module}"
    assert root.find("{*}scm/{*}url") is not None, f"Missing SCM in {module}"
    print(f"OK: {GROUP}:{module}:{version}")

root = ET.parse(Path("bedrock-connection/build/publications/maven/pom-default.xml")).getroot()
deps = root.findall(".//{*}dependency")
assert any(
    dep.findtext("{*}groupId") == GROUP
    and dep.findtext("{*}artifactId") == "bedrock-codec"
    and dep.findtext("{*}version") == EXPECTED["bedrock-codec"]
    for dep in deps
), "bedrock-connection must reference the matching bedrock-codec publication"
print("OK: bedrock-connection -> bedrock-codec")

