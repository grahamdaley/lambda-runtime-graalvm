
# Lambda Runtime Graalvm

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Build Passing](https://github.com/grahamdaley/lambda-runtime-graalvm/actions/workflows/ci.yml/badge.svg)](https://github.com/grahamdaley/lambda-runtime-graalvm/actions)
[![Download](https://img.shields.io/maven-central/v/io.github.grahamdaley/lambda-runtime-graalvm "Download lambda-runtime-graalvm from Maven Central")](https://search.maven.org/search?q=g:io.github.grahamdaley%20AND%20a:lambda-runtime-graalvm)


Lambda Runtime Graalvm is a Java Library that makes it easy to handle [AWS Lambdas](https://aws.amazon.com/lambda/) written in [Java](https://www.java.com) and compiled to native executable code using [Graalvm](https://www.graalvm.org/). 

Benefits of using [Graalvm](https://www.graalvm.org/) over [Java](https://www.java.com):
 * Much faster startup time (seconds to milliseconds)
 * Much lower memory usage

This library is a fork of the original [lambda-runtime-graalvm](https://github.com/formkiq/lambda-runtime-graalvm).
The purpose of the original library was to allow GraalVM-compiled Lambda functions to run using the standard
AWS OS-only runtime.

This fork was created to allow the library to be used with GraalVM-compiled Lambda functions that are hosted on
a custom image. The benefits of doing this are:
 * No need for reflection any more in the library
 * Compiled Lambda function code can now be larger than the 50MB zipped / 250MB unzipped limit.

## Tutorial

 Tutorial for the original library: https://blog.formkiq.com/tutorials/aws-lambda-graalvm

