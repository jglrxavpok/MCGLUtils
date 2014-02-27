package org.jglrxavpok.glutils;

import cpw.mods.fml.common.eventhandler.Event;

public class TessellatorModelEvent extends Event
{

	public static class RenderPre extends TessellatorModelEvent
	{

		public RenderPre(TessellatorModel model)
		{
			super(model);
		}

	}
	
	public TessellatorModel model;

	public TessellatorModelEvent(TessellatorModel model)
	{
		this.model = model;
	}

	public static class RenderGroupEvent extends TessellatorModelEvent
	{

	    public static class MaterialUnapplyEvent extends RenderGroupEvent
        {

            public GLMaterial material;
            public GL_Triangle triangle;

            public MaterialUnapplyEvent(TessellatorModel model, String group, GLMaterial mtl, GL_Triangle t)
            {
                super(group, model);
                this.material = mtl;
                this.triangle = t;
            }
        }

        public String group;

		public RenderGroupEvent(String groupName, TessellatorModel model)
		{
			super(model);
			this.group = groupName;
		}

        public static class MaterialApplyEvent extends RenderGroupEvent
        {

            public GLMaterial material;
            public GL_Triangle triangle;

            public MaterialApplyEvent(TessellatorModel model, String group, GLMaterial mtl, GL_Triangle t)
            {
                super(group, model);
                this.material = mtl;
                this.triangle = t;
            }
        }
        
        
		public static class Pre extends RenderGroupEvent
		{
			public Pre(String g, TessellatorModel m)
			{
				super(g,m);
			}
		}

		public static class Post extends RenderGroupEvent
		{
			public Post(String g, TessellatorModel m)
			{
				super(g,m);
			}
		}

	}
	
	public boolean isCancelable()
	{
		return true;
	}

}
