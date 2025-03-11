import sbt.Keys._
import sbt._
import wartremover.Wart
import wartremover.WartRemover.autoImport.{Warts, wartremoverErrors, wartremoverExcluded}

object WartRemoverSettings {

  lazy val wartRemoverSettings =
    Seq(
      (Compile / compile / wartremoverErrors) ++= Warts.allBut(
        Wart.DefaultArguments,
        Wart.Equals,
        Wart.ImplicitConversion,
        Wart.ImplicitParameter,
        Wart.Nothing,
        Wart.Overloading,
        Wart.SeqApply,
        Wart.Throw,
        Wart.ToString
      ),
      Test / compile / wartremoverErrors --= Seq(
        Wart.Any,
        Wart.GlobalExecutionContext,
        Wart.Null,
        Wart.NonUnitStatements,
        Wart.PublicInference
      ),
      wartremoverExcluded ++= (baseDirectory.value ** "*.sc").get
    )

}
