import $ivy.`de.tototec::de.tobiasroeser.mill.vcs.version::0.2.0`
import de.tobiasroeser.mill.vcs.version.VcsVersion
import $ivy.`io.github.davidgregory084::mill-tpolecat::0.3.0`
import io.github.davidgregory084.TpolecatModule

import mill.scalalib.scalafmt.ScalafmtModule
import mill._
import mill.modules.Jvm
import mill.scalalib._
import mill.scalalib.api.Util._
import mill.scalalib.publish._

trait BaseModule extends Module {
  def millSourcePath: os.Path = {
    val originalRelativePath = super.millSourcePath.relativeTo(os.pwd)
    os.pwd / "modules" / originalRelativePath
  }
}

trait BaseMunitTests extends TestModule.Munit {
  def ivyDeps =
    Agg(
      ivy"org.scalameta::munit::1.0.0-M6",
      ivy"org.scalameta::munit-scalacheck::1.0.0-M6"
    )
}

trait BasePublishModule extends BaseModule with PublishModule {
  def artifactName =
    s"alloy-${millModuleSegments.parts.mkString("-")}"

  def publishVersion = VcsVersion.vcsState().format()

  def pomSettings = PomSettings(
    description = "Common Smithy Shapes",
    organization = "com.disneystreaming.alloy",
    url = "https://github.com/disneystreaming/alloy",
    licenses = Seq(),
    versionControl =
      VersionControl(Some("https://github.com/disneystreaming/alloy")),
    developers = Seq()
  )

  override def javacOptions = T {
    super.javacOptions() ++ Seq(
      "--release",
      "8"
    )
  }
}

trait BaseJavaModule extends JavaModule with BasePublishModule

trait BaseScalaModule
    extends mill.scalalib.bsp.ScalaMetalsSupport
    with ScalafmtModule
    with TpolecatModule {
  def scalaVersion = T.input("2.13.8")
  def semanticDbVersion = T.input("4.4.34")
}

object core extends BaseJavaModule {
  def ivyDeps = Agg(
    ivy"software.amazon.smithy:smithy-model:1.25.2"
  )

  /** Exclude smithy file from source jars to avoid conflict with smithy files
    * packaged into the main jar (happens when scanning the classpath via the
    * ModelAssembler if sources jars were resolved).
    */
  override def sourceJar: T[PathRef] = T {
    def underMetaInfSmithy(p: os.RelPath): Boolean =
      Seq("META-INF", "smithy").forall(p.segments.contains)

    Jvm.createJar(
      (allSources() ++ resources())
        .map(_.path)
        .filter(os.exists),
      manifest(),
      fileFilter = (_, relPath) => !underMetaInfSmithy(relPath)
    )
  }

  object test extends this.Tests with BaseScalaModule with BaseMunitTests {
    def ivyDeps = {
      super.ivyDeps() ++ Agg(
        ivy"software.amazon.smithy:smithy-aws-traits:1.25.2"
      )
    }
  }
}
