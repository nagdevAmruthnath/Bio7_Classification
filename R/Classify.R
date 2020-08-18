# We use the lib package randomForest
library(randomForest)
#Predict the whole image (select your test data in the R-Shell view)!
#final<-predict(rf_model,get(.r_shell_vars[[1]]))
final<-predict(rf_model,current_feature_stack)
#We convert the votes back to numeric matrix values!
imageMatrix<-matrix(as.integer(final),imageSizeX,imageSizeY)#Create a image matrix
#Here we plot the result with R!
#image(1:imageSizeX,1:imageSizeY,imageMatrix,xlim=c(1,imageSizeX),ylim=c(imageSizeY,1),axes = T,useRaster=TRUE)

#Using the Ranger package!
#final<-predict(rf_model,current_feature_stack)
#imageMatrix<-matrix(as.integer(final$predictions),imageSizeX,imageSizeY)#Create a image matrix