package org.ocbkc.swift 
{
package snippet 
{
import _root_.scala.xml._
import _root_.net.liftweb.util._
import _root_.net.liftweb.http._
import _root_.net.liftweb.common._
import _root_.java.util.Date
import org.ocbkc.swift.lib._
import org.ocbkc.swift.OCBKC._
import Helpers._
import System.err.println
import org.ocbkc.swift.model._
import org.ocbkc.swift.global._
import org.ocbkc.swift.coord.ses._
import org.ocbkc.swift.global.Logging._
import org.ocbkc.swift.embeddedsnippet._

/** Almost the same as StudyConstitution, but now integrated as a separate round in the first session of the fresh player.
  */
class StudyConstiRound
{  log("StudyConstiRound constructor called")

   def render(ns: NodeSeq): NodeSeq =
   {
       val answer   = bind( "top", ns,
                            "studyConsti"     -> StudyConsti__ESnip.render
                            
                     )
      answer
   }
}


}
}
