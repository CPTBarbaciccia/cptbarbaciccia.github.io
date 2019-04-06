---
layout: page
title: Scripts
permalink: /scripts/
---

# Scripts Collection

[This section of the repository][look_over_here] wants to be a collection of various scripts.

## Groovy scripts

Most of these scripts were created to be used for Jenkins:
perhaps, some modifications should be necessary to use these scripts.

##### · addParameterAndShellScriptToJobs

[addParameterAndShellScriptToJobs.groovy][addParameterAndShellScriptToJobs]
adds a boolean parameter as `BooleanParameterDefinition` to jobs
that follow a naming convention (example: `it.fullName.startsWith("foobar")`),
also it adds a script to check the boolean value (through `hudson.tasks.Shell()`).
If the parameter or script (or both) are already entered, the task will skip.


[addParameterAndShellScriptToJobs]:https://github.com/x21Kenobi/x21kenobi.github.io/blob/master/assets/scripts/groovy/addParameterAndShellScriptToJobs.groovy
[look_over_here]:https://github.com/x21Kenobi/x21kenobi.github.io/blob/master/assets/scripts
