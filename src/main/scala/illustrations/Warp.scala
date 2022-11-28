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

/**
  * Warps linearly  between two shapes and writes all intermediate files to disk. 
  * Used mainly for creating visualizations for presentations.
  */
object Warp:


    def interpolate(face1 : TriangleMesh[_3D], face2 : TriangleMesh[_3D], from : Double, to : Double, numSteps : Int)(using rng : scalismo.utils.Random) : Seq[TriangleMesh[_3D]] = 
    
        val face1Downsampled = face1.operations.decimate(3000)
        
        val deformationVectors = face1.pointSet.pointIds.map(id => face2.pointSet.point(id) - face1.pointSet.point(id)).toIndexedSeq
        val df = DiscreteField3D(face1, deformationVectors.toIndexedSeq)

        for (d, i) <- (BigDecimal(0.0) to BigDecimal(1.0) by 1.0 / numSteps).zipWithIndex yield
            val inBetweenDeformations = df.map(v => v * d.toDouble).interpolate(NearestNeighborInterpolator3D())
            face1.transform(p => p + inBetweenDeformations(p))            


    def main(args : Array[String]) : Unit =

        scalismo.initialize()
        given rng : scalismo.utils.Random = scalismo.utils.Random(42)
        
        val face1 = DataRepository.loadFaceMesh(FaceId("00017_20061201_00812_neutral_face05")).get
        val face2 = DataRepository.loadFaceMesh(FaceId("00293_20080104_03413_neutral_face05")).get
       

        val meshes = interpolate(face1, face2, 0, 1, 30) 

        val outputDir = new java.io.File("./tmp/interpolated/")
        outputDir.mkdirs()

        for (mesh, i) <- meshes.zipWithIndex do 
            println("writing " +i)
            MeshIO.writeMesh(mesh, java.io.File(outputDir, s"mesh-$i.ply")).get

        

  
