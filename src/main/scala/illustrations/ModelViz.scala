/*
 * Copyright 2022 University of Basel, Department of Mathematics and Computer Science
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package illustrations

import data.DataRepository
import data.FaceId
import scalismo.common.DiscreteField3D
import scalismo.common.DiscreteField
import scalismo.common.interpolation.NearestNeighborInterpolator3D
import scalismo.mesh.TriangleMesh
import scalismo.common.PointId
import scalismo.geometry._3D
import scalismo.io.MeshIO
import scala.util.Try
import scala.collection.parallel.immutable.ParVector
import scalismo.transformations.{Translation, Rotation}
import scalismo.geometry.EuclideanVector3D
import scalismo.io.StatisticalModelIO


/**
  * Creates samples from a model and writes them to disk. . 
  * Used mainly for creating visualizations for presentations.
  */
object ModelViz:

    def main(args : Array[String]) : Unit =

        scalismo.initialize()
        given rng : scalismo.utils.Random = scalismo.utils.Random(42)
        
        
        val model = DataRepository.loadFaceModel().get
        val outputDir = new java.io.File("./tmp/samples/nose/")
        outputDir.mkdirs()

        for i <- 0 until 100 do
            val sample = model.sample()
            MeshIO.writeMesh(sample, java.io.File(outputDir, s"mesh-$i.ply")).get

