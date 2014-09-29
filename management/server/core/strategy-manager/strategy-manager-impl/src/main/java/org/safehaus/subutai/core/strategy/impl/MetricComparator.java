package org.safehaus.subutai.core.strategy.impl;


import org.safehaus.subutai.core.strategy.api.Criteria;
import org.safehaus.subutai.core.strategy.api.ServerMetric;
import org.safehaus.subutai.core.strategy.api.StrategyException;


abstract class MetricComparator
{

    static MetricComparator create( Criteria criteria ) throws StrategyException
    {
        MetricComparator mc = null;
        if ( "MORE_HDD".equals( criteria.getId() ) )
        {
            mc = new MetricComparator()
            {
                @Override
                public int getValue( ServerMetric m )
                {
                    return m.getFreeHddMb();
                }
            };
        }
        if ( "MORE_RAM".equals( criteria.getId() ) )
        {
            mc = new MetricComparator()
            {
                @Override
                int getValue( ServerMetric m )
                {
                    return m.getFreeRamMb();
                }
            };
        }
        if ( "MORE_CPU".equals( criteria.getId() ) )
        {
            mc = new MetricComparator()
            {
                @Override
                int getValue( ServerMetric m )
                {
                    return m.getCpuLoadPercent();
                }


                @Override
                boolean isLessBetter()
                {
                    return true;
                }
            };
        }
        else
        {
            throw new StrategyException(
                    String.format( "Comparator not defined for criteria [%s]", criteria.getId() ) );
        }
        return mc;
    }


    abstract int getValue( ServerMetric m );


    boolean isLessBetter()
    {
        return false;
    }
}
