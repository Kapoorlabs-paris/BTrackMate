package pluginTools.simplifiedio;

import java.io.File;
import java.util.StringJoiner;

import org.scijava.util.FileUtils;

import ij.IJ;
import ij.ImagePlus;
import ij.io.Opener;
import io.scif.SCIFIO;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.DefaultLinearAxis;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converters;
import net.imglib2.converter.RealTypeConverters;
import net.imglib2.img.ImagePlusAdapter;
import net.imglib2.img.Img;
import net.imglib2.img.ImgView;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.display.imagej.ImgToVirtualStack;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

public class SimplifiedIO {

	private static SCIFIO scifio;

	/**
	 * Loads an image using ImageJ1, then wraps it into an ImgPlus object
	 * Returns null if the image is not in a supported format.
	 * Quick and dirty it either works or does not
	 *
	 * @see net.imagej.ImgPlus
	 * @see ij.IJ#openImage(String)
	 */
	@SuppressWarnings( { "rawtypes" } )
	static ImgPlus openImageWithIJ1( final String path ) {
		// package private to allow testing
		final Opener opnr = new Opener();
		ImagePlus image = opnr.openImage( path );
		if ( image == null )
			throw new SimplifiedIOException( "new ij.io.Opener().openImage() returned null." );
		return ImagePlusAdapter.wrapImgPlus( image );
	}



	/**
	 * Loads an image using BioFormats
	 *
	 * @see net.imagej.ImgPlus
	 */


	/**
	 * Loads an image into an ImgPlus object
	 *
	 * @see net.imagej.ImgPlus
	 */
	@SuppressWarnings( "rawtypes" )
	public static ImgPlus openImage( final String path ) {

		StringJoiner messages = new StringJoiner( "\n" );

		try {
			return SimplifiedIO.openImageWithIJ1( path );
		} catch ( Exception e ) {
			messages.add( "ImageJ1 Exception: " + e.getMessage() );
		}

		if ( !new File( path ).exists() )
			throw new SimplifiedIOException( "Image file doesn't exist: " + path );

		throw new SimplifiedIOException( "Couldn't open image file: \"" + path + "\"\n" + "Exceptions:\n" + messages );
	}

	public static < T extends NativeType< T > > RandomAccessibleInterval< T > openImage( String path, T type ) {
		return convert( openImage( path ), type );
	}

	@SuppressWarnings( { "rawtypes", "unchecked" } )
	public static < T extends NativeType< T > > RandomAccessibleInterval< T > convert( ImgPlus image, T type ) {
		Object imageType = Util.getTypeFromInterval( image );
		if ( imageType.getClass().equals( type.getClass() ) ) {
			return image;
		} else if ( imageType instanceof RealType && type instanceof RealType ) {
			return convertBetweenRealType( image, ( RealType ) type );
		} else if ( imageType instanceof UnsignedByteType && type instanceof ARGBType ) {
			return UNconvertToRGB( image );
		} else if ( imageType instanceof ARGBType && type instanceof RealType ) { return convertARGBTypeToRealType( image, ( RealType ) type ); }
		
		else if (imageType instanceof FloatType)
			
			return  convertToRGB(image);
		
			throw new IllegalStateException( "Cannot convert between given pixel types: " + imageType.getClass().getSimpleName() + ", " + type.getClass().getSimpleName() );
	}
	public static  RandomAccessibleInterval<ARGBType> UNconvertToRGB(RandomAccessibleInterval<UnsignedByteType> image) {
		RandomAccessibleInterval< ARGBType > convertedRAI = new ArrayImgFactory().create(image, new ARGBType());
		Cursor<UnsignedByteType> cur = Views.iterable(image).localizingCursor();
		RandomAccess<ARGBType> ran = convertedRAI.randomAccess();
		
		while(cur.hasNext()) {
			
			
			cur.fwd();
			int value =  cur.get().get();
			ran.setPosition(cur);
			ran.get().set(ARGBType.rgba( value, value, value, 255 ));
			
			
		}
		
		return convertedRAI;
		
	}

	
	public static  RandomAccessibleInterval<ARGBType> convertToRGB(RandomAccessibleInterval<FloatType> image) {
		RandomAccessibleInterval< ARGBType > convertedRAI = new ArrayImgFactory().create(image, new ARGBType());
		Cursor<FloatType> cur = Views.iterable(image).localizingCursor();
		RandomAccess<ARGBType> ran = convertedRAI.randomAccess();
		
		while(cur.hasNext()) {
			
			
			cur.fwd();
			int value = (int) cur.get().get();
			ran.setPosition(cur);
			ran.get().set(ARGBType.rgba( value, value, value, 255 ));
			
			
		}
		
		return convertedRAI;
		
	}

	/**
	 * Saves the specified image to the specified file path.
	 * The specified image is saved as a "tif" if there is no extension.
	 * <p>
	 * The method also accepts {@link ImgPlus} and stores the metadata.
	 **/
	public static void saveImage( RandomAccessibleInterval< ? > img, String path ) {
		path = addTifAsDefaultExtension( path );
		IJ.save( ImgToVirtualStack.wrap( toImgPlus( img ) ), path );
	}

	@SuppressWarnings( { "unchecked", "rawtypes" } )
	private static ImgPlus< ? > toImgPlus( RandomAccessibleInterval< ? > image ) {
		if ( image instanceof ImgPlus )
			return ( ImgPlus< ? > ) image;
		if ( image instanceof Img )
			return new ImgPlus<>( ( Img< ? > ) image );
		return new ImgPlus<>( ImgView.wrap( ( RandomAccessibleInterval ) image, null ) );
	}

	private static String addTifAsDefaultExtension( String path ) {
		String ext = FileUtils.getExtension( path ).toLowerCase();

		if ( ext.isEmpty() ) {
			path += ".tif";
		}
		return path;
	}



	@SuppressWarnings( { "unchecked", "rawtypes" } )
	private static < T extends NativeType< T > > ImgPlus< T >
			convertBetweenRealType( ImgPlus image, RealType type ) {
		RandomAccessibleInterval< T > convertedRAI = RealTypeConverters.convert( image, type );
		Img< T > convertedImg = ImgView.wrap( convertedRAI, new ArrayImgFactory<T>( convertedRAI.randomAccess().get().createVariable() ) );
		return new ImgPlus<>( convertedImg, image );
	}

	@SuppressWarnings("rawtypes")
	private static < T extends NativeType< T > > ImgPlus< T > convertARGBTypeToRealType( ImgPlus< ARGBType > image,
			RealType type) {
		final ImgPlus< T > imgPlus = convertBetweenRealType(
				new ImgPlus<>( ImgView.wrap( Converters.argbChannels( image ), null ) ), type );

		int n = image.numDimensions();
		for ( int i = 0; i < n; i++ )
		{
			imgPlus.setAxis( image.axis( i ), i );
		}
		imgPlus.setAxis( new DefaultLinearAxis( Axes.CHANNEL ), n );
		imgPlus.setName( image.getName() );
		return imgPlus;
	}

	private static SCIFIO getScifio() {
		if ( scifio == null )
			scifio = new SCIFIO();
		return scifio;
	}
}
