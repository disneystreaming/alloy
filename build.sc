import $ivy.`de.tototec::de.tobiasroeser.mill.vcs.version::0.4.1`
import de.tobiasroeser.mill.vcs.version.VcsVersion
import $ivy.`io.github.davidgregory084::mill-tpolecat::0.3.5`
import $ivy.`com.lewisjkl::header-mill-plugin::0.0.4`
import header._
import $file.plugins.ci.CiReleaseModules
import CiReleaseModules.{CiReleaseModule, SonatypeHost, ReleaseModule, Discover}
import io.github.davidgregory084.TpolecatModule
import $ivy.`com.github.lolgab::mill-mima::0.1.1`
import com.github.lolgab.mill.mima._

import mill.scalalib.scalafmt.ScalafmtModule
import mill._
import mill.modules.Jvm
import mill.scalalib._
import mill.scalalib.publish._
import mill.define.ExternalModule
import mill.eval.Evaluator

object InternalReleaseModule extends Module {

  /** This is a replacement for the mill.scalalib.PublishModule/publishAll task
    * that should basically work identically _but_ without requiring the user to
    * pass in anything. It also sets up your gpg stuff and grabs the necessary
    * env variables to publish to sonatype for you.
    */
  def publishAll(ev: Evaluator): Command[Unit] = {
    ReleaseModule.publishAll(ev)
  }

  import Discover._
  lazy val millDiscover: mill.define.Discover[this.type] =
    mill.define.Discover[this.type]
}

trait BaseModule extends Module with HeaderModule {
  def millSourcePath: os.Path = {
    val originalRelativePath = super.millSourcePath.relativeTo(os.pwd)
    os.pwd / "modules" / originalRelativePath
  }

  def includeFileExtensions: List[String] = List("scala", "java")
  def license: HeaderLicense = HeaderLicense.Custom("""|Copyright 2022 Disney Streaming
                                                       |
                                                       |Licensed under the Tomorrow Open Source Technology License, Version 1.0 (the "License");
                                                       |you may not use this file except in compliance with the License.
                                                       |You may obtain a copy of the License at
                                                       |
                                                       |   https://disneystreaming.github.io/TOST-1.0.txt
                                                       |
                                                       |Unless required by applicable law or agreed to in writing, software
                                                       |distributed under the License is distributed on an "AS IS" BASIS,
                                                       |WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
                                                       |See the License for the specific language governing permissions and
                                                       |limitations under the License.
                                                       |""".stripMargin)
}

trait BaseMunitTests extends TestModule.Munit {
  def ivyDeps = Deps.munit.all
}

trait BasePublishModule extends BaseModule with CiReleaseModule {

  override def publishVersion: T[String] = T {
    if (isCI()) super.publishVersion() else "dev-SNAPSHOT"
  }

  def isCI = T.input(T.ctx().env.contains("CI"))

  def artifactName =
    s"alloy-${millModuleSegments.parts.mkString("-")}"

  override def sonatypeHost = Some(SonatypeHost.s01)

  def pomSettings = PomSettings(
    description = "Common Smithy Shapes",
    organization = "com.disneystreaming.alloy",
    url = "https://github.com/disneystreaming/alloy",
    licenses = Seq(
      License(
        id = "TOST-1.0",
        name = "TOMORROW OPEN SOURCE TECHNOLOGY LICENSE 1.0",
        url = "https://disneystreaming.github.io/TOST-1.0.txt",
        isOsiApproved = false,
        isFsfLibre = false,
        distribution = "repo"
      )
    ),
    versionControl =
      VersionControl(Some("https://github.com/disneystreaming/alloy")),
    developers = Seq(
      Developer("lewisjkl", "Jeff Lewis", "http://github.com/lewisjkl")
    )
  )

  override def javacOptions = T {
    super.javacOptions() ++ Seq(
      "--release",
      "8"
    )
  }
}

trait BaseJavaModule extends JavaModule with BasePublishModule

trait BaseScalaNoPublishModule
    extends ScalaModule
    with ScalafmtModule
    with TpolecatModule {
  def scalaVersion = T.input("2.13.15")
}

trait BaseMimaModule extends BasePublishModule with Mima {
  def mimaPreviousVersions = Seq("0.2.0")
}

trait BaseScalaModule extends BaseScalaNoPublishModule with BaseMimaModule

trait BaseCrossScalaModule
    extends ScalaModule
    with ScalafmtModule
    with TpolecatModule
    with CrossScalaModule
    with BaseMimaModule

object core extends BaseJavaModule {
  def ivyDeps = Agg(
    Deps.smithy.model
  )

  /** Exclude smithy file from source jars to avoid conflict with smithy files
    * packaged into the main jar (happens when scanning the classpath via the
    * ModelAssembler if sources jars were resolved).
    */
  override def sourceJar: T[PathRef] = T {
    def underMetaInfSmithy(p: os.RelPath): Boolean =
      Seq("META-INF", "smithy").forall(p.segments.contains)

    mill.util.Jvm.createJar(
      (allSources() ++ resources())
        .map(_.path)
        .filter(os.exists),
      manifest(),
      fileFilter = (_, relPath) => !underMetaInfSmithy(relPath)
    )
  }

  object test
      extends JavaModuleTests
      with BaseScalaNoPublishModule
      with BaseMunitTests {
    def ivyDeps = {
      super.ivyDeps() ++ Agg(
        Deps.smithy.awsTraits
      )
    }
  }
}

object protobuf extends BaseJavaModule {}

val scalaVersionsMap =
  Map("2.13" -> "2.13.15", "2.12" -> "2.12.17", "3" -> "3.3.0")
object openapi extends Cross[OpenapiModule](scalaVersionsMap.keys.toList)
trait OpenapiModule extends BaseCrossScalaModule {
  val crossVersion = crossValue
  def artifactName = "alloy-openapi"

  def crossScalaVersion = scalaVersionsMap(crossVersion)
  def scalacOptions = T {
    val base = super.scalacOptions()
    if (crossVersion == "3")
      base.filterNot(Set("-Werror", "-Ykind-projector"))
    else base
  }

  def moduleDeps = Seq(core)

  def ivyDeps = Agg(
    Deps.scala.compat,
    Deps.smithy.openapi,
    Deps.cats.core
  )

  object test extends ScalaTests with BaseMunitTests

  override def mimaBinaryIssueFilters = super.mimaBinaryIssueFilters() ++ Seq(
    ProblemFilter.exclude[MissingClassProblem](
      "alloy.openapi.DiscriminatedUnions"
    )
  )
}

object `protocol-tests` extends BaseJavaModule {
  def moduleDeps = Seq(core)

  def ivyDeps = Agg(
    Deps.smithy.awsProtocolTestTraits,
    Deps.smithy.awsTests
  )

  object sanity
      extends BaseScalaNoPublishModule
      with JavaModuleTests
      with TestModule.Munit {
    def ivyDeps = Agg(Deps.munit.munit)
  }
}

object Deps {
  val smithy = new {
    val smithyVersion = "1.49.0"
    val model = ivy"software.amazon.smithy:smithy-model:$smithyVersion"
    val awsTraits = ivy"software.amazon.smithy:smithy-aws-traits:$smithyVersion"
    val awsProtocolTestTraits =
      ivy"software.amazon.smithy:smithy-protocol-test-traits:$smithyVersion"
    val awsTests =
      ivy"software.amazon.smithy:smithy-aws-protocol-tests:$smithyVersion"
    val openapi = ivy"software.amazon.smithy:smithy-openapi:$smithyVersion"
  }

  val cats = new {
    val core = ivy"org.typelevel::cats-core:2.12.0"
  }

  val scala = new {
    val compat = ivy"org.scala-lang.modules::scala-collection-compat:2.12.0"
  }

  val munit = new {
    val munit = ivy"org.scalameta::munit::1.0.4"
    val scalaCheck = ivy"org.scalameta::munit-scalacheck::1.0.0"
    val all = Agg(munit, scalaCheck)
  }
}
