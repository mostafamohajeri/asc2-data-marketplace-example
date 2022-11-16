package io.github.mostafamohajeri
package normativeservices

import bb.{IBeliefBase, IBeliefBaseFactory}
import bb.expstyla.exp.GenericTerm

class EFlintBBFactory(file: String) extends IBeliefBaseFactory {
  override def apply(): IBeliefBase[GenericTerm] = new EflintBeliefBase(file)
}
