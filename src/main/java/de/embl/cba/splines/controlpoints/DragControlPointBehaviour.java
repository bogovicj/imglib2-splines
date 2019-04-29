package de.embl.cba.splines.controlpoints;

import bdv.tools.boundingbox.AbstractTransformedBoxModel;
import net.imglib2.FinalRealInterval;
import net.imglib2.RealInterval;
import net.imglib2.realtransform.AffineTransform3D;
import org.scijava.ui.behaviour.DragBehaviour;

final class DragControlPointBehaviour implements DragBehaviour
{
	private final ControlPointsOverlay pointsOverlay;

	private boolean moving = false;

	private final AbstractTransformedBoxModel model;

	private final double[] initMin = new double[ 3 ];

	private final double[] initMax = new double[ 3 ];

	private final double[] initCorner = new double[ 3 ];

	private int cornerId;

	public DragControlPointBehaviour( final ControlPointsOverlay pointsOverlay, final AbstractTransformedBoxModel model )
	{
		this.pointsOverlay = pointsOverlay;
		this.model = model;
	}

	@Override
	public void init( final int x, final int y )
	{
		cornerId = pointsOverlay.getHighlightedCornerIndex();
		if ( cornerId < 0 )
			return;

		final RealInterval interval = model.getInterval();
		IntervalCorners.corner( interval, cornerId, initCorner );
		interval.realMin( initMin );
		interval.realMax( initMax );

		moving = true;
	}

	private final AffineTransform3D transform = new AffineTransform3D();

	@Override
	public void drag( final int x, final int y )
	{
		if ( !moving )
			return;

		pointsOverlay.getBoxToViewerTransform( transform );
		final double[] gPos = new double[ 3 ];
		transform.apply( initCorner, gPos );
		final double[] lPos = pointsOverlay.renderBoxHelper.reproject( x, y, gPos[ 2 ] );
		transform.applyInverse( gPos, lPos );

		final double[] min = new double[ 3 ];
		final double[] max = new double[ 3 ];
		for ( int d = 0; d < 3; ++d )
		{
			final double p = gPos[ d ];
			if ( ( cornerId & ( 1 << d ) ) == 0 )
			{
				min[ d ] = p;
				max[ d ] = initMax[ d ] = Math.max( initMax[ d ], p );
			}
			else
			{
				min[ d ] = initMin[ d ] = Math.min( initMin[ d ], p );
				max[ d ] = p;
			}
		}

		model.setInterval( new FinalRealInterval( min, max ) );
	}

	@Override
	public void end( final int x, final int y )
	{
		moving = false;
	}
}
