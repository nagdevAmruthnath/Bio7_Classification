file = getArgument() //We can call this macro from Java with an argument!
//run("Bio-Formats (Windowless)", "open=" + file);
last=file.lastIndexOf("_B");
sub=file.substring(0,last+2);
layers="1,2";
//print(sub);

run("Bio-Formats", "open="+ file+" autoscale color_mode=Default group_files rois_import=[ROI manager] view=Hyperstack stack_order=XYCZT axis_1_number_of_images=11 axis_1_axis_first_image=1 axis_1_axis_increment=1 contains=[] name="+sub+"<"+layers+">.TIF");
//run("Scale...", "x=0.5 y=0.5 width=3856 height=3916 interpolation=Bilinear average create");

