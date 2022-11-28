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

package modelling

import scalismo.utils.Random
import scalismo.geometry._3D
import scalismo.kernels.GaussianKernel3D
import scalismo.statisticalmodel.GaussianProcess
import scalismo.kernels.DiagonalKernel3D
import scalismo.geometry.EuclideanVector
import scalismo.statisticalmodel.LowRankGaussianProcess
import data.DataRepository
import scalismo.common.interpolation.TriangleMeshInterpolator3D
import scalismo.statisticalmodel.PointDistributionModel
import scalismo.ui.api.ScalismoUI
import scalismo.mesh.TriangleMesh

/**
  * Augments the face model with a GP-model for smooth deformations. This enhances the flexibilty
  * of the face model. 
  */
object AugmentModel:
  
    def augmentModel(pdm : PointDistributionModel[_3D, TriangleMesh]) : PointDistributionModel[_3D, TriangleMesh] = 
        val biasCov = DiagonalKernel3D(GaussianKernel3D(100.0, scaleFactor = 1), outputDim = 3)
        val biasModel = GaussianProcess[_3D, EuclideanVector[_3D]](biasCov)
        val lowRankBias = LowRankGaussianProcess.approximateGPCholesky(pdm.reference, biasModel, 1e-5, TriangleMeshInterpolator3D())
        PointDistributionModel.augmentModel(pdm, lowRankBias)


    def main(args : Array[String]) : Unit = 

        val ui = ScalismoUI()

        given rng : Random = Random(42)
        scalismo.initialize()

        val pdm = DataRepository.loadFaceModel().get
        val augmentedModel = augmentModel(pdm)
        DataRepository.writeAugmentedFaceModel(augmentedModel)
        
        ui.show(augmentedModel, "augmented-model")