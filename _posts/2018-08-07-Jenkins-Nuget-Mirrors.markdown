---
layout: post
title:  "Jenkins, Xamarin and the wrong way to do EE CI"
date:   2018-08-07 23:00:00
category: devops
tags: [ jenkins, pipeline, nuget, groovy, misadventures ]
---

To date, I'm living a strange situation where there are software that wants you must reach their online repositories to execution a runtime-compilation and together there companies that wants close their infrastructure for fear of malicious intrusion.

I feel to haven't enough <experience/culture> about this argument, so I don't know if these contrasts of wanting "all (the code) open for policies reasons but at the same time all (the code) closed for security reason" be a good thing.

For me it's frustrating, because I understand both reasons - and this isn't the post to discuss about it.

### Scenario

Imagine you are working with a closed infrastructure, which have a local network connections and no way (literally no way) of reach the internet.
Then, imagine you have to manage a [Xamarin][xamarin] project and the development group decide to using a Xamarin-Nuget plugin that could has in the configurations something like this:

`packages/Xamarin.something.version/build/framework/Xamarin.something.targets`
{% highlight xml%}
<ItemGroup>
    <XamarinOtherComponent Include="$(_xxxx_componentfile)">
        <!-- OH, look here -->
        <Url>https://dl.google.com/url/path/to/file.ext</Url>
        <!-- OH, look here -->
        <ToFile>file.ext</ToFile>
        <Kind>Uncompressed</Kind>
        <Md5>ThisIsMd5Hash</Md5>
    </XamarinOtherComponent>
</ItemGroup>
{% endhighlight %}

Also and as if this weren't enough, *imagine to have manage all projects with [Jenkins][jenkins] and [Jenkins-pipeline][groovy-pipeline] feature as CI solution.*
The build will end up in error for sure, because during the runtime will not be possible download the packages.

Are you starting to feel scared!? 👻

 1. The first (**GOOD**) idea would be read the manuals and looking for an option to override that key with a mirror url: a side nuget sources, to date there aren't similar options!
 2. The second (**WRONG**) idea you should (**NOT**) have, it would be to manually download the nuget package, edit the configuration so to point locally (like a repository as may be `webdav`) and then put it "in that point".
 3. The second (**AND MORE WRONG**) idea, it would be saying to development group of integrate the nuget package, or its source code, directly in the their project code.

Now, if you aren't confident with Groovy, C#, etc, you should be scared!

### Solution

As DevOps and considering that previously written,
in my opinion, the cleanest way it would be create an "overrider": a function which do a "check" to verify and (if necessary) make a substitution of the url in the `.target` configurations, just after `nuget restore` step.

In this way, you can also put this function in a shared-library or in any case remove it without conflicts.

So, may be something like this:
{% highlight groovy%}
stage('Nuget Restore') {
  steps {
    script {
      bat("${NUGET_EXE} restore -Source ${NUGET_SOURCE_URL} -Verbosity normal")

      def targetsFiles = findFiles(glob: 'packages/**/*.targets')
      targetsFiles.length.times {
        if (Checker(readFile(targetsFiles[it].path), '<Url>https://dl.google.com(.+)</Url>')) {
          def inputTargetFile = readFile(targetsFiles[it].path).replaceAll("https://dl.google.com", "http://example.mirror.url/path")
          writeFile(file: targetsFiles[it].path, text: inputTargetFile, encoding: 'UTF-8')
        }
      }
    }
  }
}
{% endhighlight %}

Where `Checker` is another function like this:
{% highlight groovy%}
def Checker(xmlfile, regex) {
    def matcher = xmlfile =~ regex
    matcher ? matcher[0][1] : null
}
{% endhighlight %}

### Considerations

Cases like these (unfortunately) are more common that anyone believes, so it's even more mandatory have a plan and standard operating procedures to better manage these unexpected events.

My advice is to have as many internet access as possible combined with control tools for malicious software.
Because, hold all infrastructure closed and at the same time want to use packages that comes from the internet anyway, doesn't prevent external attacks and doesn't reduces its chances significantly.

Also, after a particular experience, I'm of opinion that each software tool (which needs a internet connection) should have the possibility to using mirrors url.

### Troubleshooting

If you decide to use mine solution, you could have an exception error caused by [Jenkins Groovy Sandbox][Script+Security+Plugin]:

{% highlight groovy%}
org.jenkinsci.plugins.scriptsecurity.sandbox.RejectedAccessException: Scripts not permitted to use staticMethod org.codehaus.groovy.runtime.DefaultGroovyMethods times java.lang.Number groovy.lang.Closure
{% endhighlight %}

In this case, you must go in your Jenkins in [In-process Script Approval][In-process_Script_Approval] section (es: http://yourjenkins.url/**scriptApproval**/) for approve the following signature:
`method groovy.lang.GroovyObject invokeMethod java.lang.String java.lang.Object`


[jenkins]: https://jenkins.io/
[groovy-pipeline]: https://jenkins.io/doc/book/pipeline/
[xamarin]: https://visualstudio.microsoft.com/xamarin/
[Script+Security+Plugin]: https://wiki.jenkins.io/display/JENKINS/Script+Security+Plugin
[In-process_Script_Approval]: https://jenkins.io/doc/book/managing/script-approval
