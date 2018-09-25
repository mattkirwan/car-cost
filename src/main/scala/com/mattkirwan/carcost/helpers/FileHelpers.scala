package com.mattkirwan.carcost.helpers

import java.io.{BufferedWriter, File, FileWriter}

import com.mattkirwan.carcost.helpers.Control.using

import scala.io.Source
import scala.util.Try

object FileHelpers {

  def readFileWithTry(filename: String): Try[List[String]] = {
    Try {
      val lines = using(Source.fromFile(filename)) { source =>
        (for (line <- source.getLines) yield line).toList
      }
      lines
    }
  }

  def writeFileWithTry(filename: String, content: String): Try[String] = {
    val file = new File(filename)
    Try {
      using(new BufferedWriter(new FileWriter(file))) { bufferedWriter =>
        bufferedWriter.write(content)
      }
      filename
    }
  }

}