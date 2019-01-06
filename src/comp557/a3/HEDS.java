package comp557.a3;
//Andi Dai 260844907

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector4d;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;

/**
 * Half edge data structure.
 * Maintains a list of faces (i.e., one half edge of each) to allow
 * for easy display of geometry.
 */
public class HEDS {

    /** List of faces */
    Set<Face> faces = new HashSet<Face>();
    Queue<Edge> queue = new PriorityQueue<Edge>();
	float weight;

    /**
     * Constructs an empty mesh (used when building a mesh with subdivision)
     */
    public HEDS() {
        // do nothing
    }
        
    /**
     * Builds a half edge data structure from the polygon soup   
     * @param soup
     */
    public HEDS( PolygonSoup soup ) {
        halfEdges.clear();
        faces.clear();
        
        // TODO: Objective 1: create the half edge data structure from the polygon soup    
        //soup.vertexList;
		//soup.faceList;
        
        for ( int[] faceVertex : soup.faceList ) {        	
			
			int i,j;
			for ( int k = 0; k < faceVertex.length; k++ ) {			
				HalfEdge he = new HalfEdge();
				if(k < faceVertex.length-1){
					i = faceVertex[k];
					j = faceVertex[k+1];
				}else{
					i = faceVertex[k];
					j = faceVertex[0];
				}
				//find head and tail
				he.head = soup.vertexList.get(j);//set head
				halfEdges.put(""+ i + "," + j, he);//set key
				
				if(halfEdges.get(""+ j + "," + i) != null){
					he.twin = halfEdges.get(""+ j + "," + i);
					halfEdges.get(""+ j + "," + i).twin = he;
					he.e = he.twin.e;
				}
				else
				{
						he.e = new Edge();
						he.e.he = he;				
				}
			}//set edge&twin
			
			for ( int k = 0; k < faceVertex.length; k++ ) {				
				HalfEdge chosenedge = new HalfEdge();
				
				if(k < faceVertex.length-2){
					i = faceVertex[k];
					j = faceVertex[k+1];
					chosenedge = halfEdges.get(""+ i + "," + j);					
					chosenedge.next = halfEdges.get(""+ j + "," + faceVertex[k+2]);
				}else if(k < faceVertex.length-1){
					i = faceVertex[k];
					j = faceVertex[k+1];
					chosenedge = halfEdges.get(""+ i + "," + j);
					chosenedge.next = halfEdges.get(""+ j + "," + faceVertex[0]);
				}else{
					i = faceVertex[k];
					j = faceVertex[0];
					chosenedge = halfEdges.get(""+ i + "," + j);
					chosenedge.next = halfEdges.get(""+ j + "," + faceVertex[1]);					
				}
			}			//set next
			//Add face 
			Face f = new Face(halfEdges.get(""+ faceVertex[0] + "," + faceVertex[1]));
			faces.add(f);
		}

        
        
        
        // TODO: Objective 5: fill your priority queue on load
        Fillqueue();
        
    }

    public void Fillqueue() {
		// TODO Auto-generated method stub
    	for ( Face face : faces ) {
			HalfEdge he = face.he;
			HalfEdge he1 = he;
			do {
				updatevertexQ(he1);
				Point3d x=OptimalVPosition(he1);
				he1.e.v.x=x.x;
				he1.e.v.y=x.y;
				he1.e.v.z=x.z;
				he1.e.v.w = 1;
				he1.e.error = EdgeError(he1);
				if (!queue.contains(he1.e)){
					queue.add(he1.e);
				}
				he1 = he1.next;
			} while ( he1 != he );
    	}
	}

	/**
     * You might want to use this to match up half edges... 
     */
    Map<String,HalfEdge> halfEdges = new TreeMap<String,HalfEdge>();
    
    
    // TODO: Objective 2, 3, 4, 5: write methods to help with collapse, and for checking topological problems
    public boolean Collapse(HalfEdge collapseedge) {
    	if (! redoListHalfEdge.isEmpty() )
    		{redoCollapse();
    		return true;
    		}// if can redo then collapse=redo
    	else
    		
    	{
    	
    	if (!IsCollapsible(collapseedge)) 
			{System.out.println("Can Not Collapse");
    	    return false;}
    	else
    	{
    		Vertex newvertex=new Vertex();
    		newvertex.p=OptimalVPosition(collapseedge);
    	HalfEdge A = collapseedge.next.twin;
    	HalfEdge B = collapseedge.next.next.twin;
    	HalfEdge C = collapseedge.twin.next.next.twin;
    	HalfEdge D = collapseedge.twin.next.twin;



    	undoList.add(collapseedge);

    	
    	
    	queue.remove(collapseedge.e);
    	queue.remove(A.e);
    	queue.remove(B.e);
    	queue.remove(C.e);
    	queue.remove(D.e);
    	
    	
    	A.twin = B;
    	B.twin = A;
    	C.twin = D;
    	D.twin = C;

    	
    	
    	faces.remove(collapseedge.leftFace);
    	faces.remove(collapseedge.twin.leftFace);
    	


    	
    	HalfEdge loop = A;
    	do{    		
    		loop.head =newvertex;   		
    		loop = loop.next.twin;
    				
    	}while(loop!=A);
    	B.e=A.e;
    	D.e=C.e;
    	A.e.he=A;
    	A.twin.e.he=A;
    	C.e.he=C;
    	C.twin.e.he=C;
    	
    	for(Face face:faces)
    	{
    		face.recomputeNormal();    		
    	}

    	//vertexQ(A);
    	newvertex.Q=collapseedge.e.Q;


    	Point3d p1=OptimalVPosition(A);
		A.e.v.x=p1.x;
		A.e.v.y=p1.y;
		A.e.v.z=p1.z;
		A.e.v.w = 1;
		A.e.error = EdgeError(A);
		
    	Point3d p2=OptimalVPosition(C);
		C.e.v.x=p2.x;
		C.e.v.y=p2.y;
		C.e.v.z=p2.z;
		C.e.v.w = 1;
		C.e.error = EdgeError(C);
		
		

		loop = A;
    	do{    		
    		Point3d p3=OptimalVPosition(loop);
    		loop.e.v.x=p3.x;
    		loop.e.v.y=p3.y;
    		loop.e.v.z=p3.z;
    		loop.e.v.w = 1;
    		loop.e.error = EdgeError(loop);
    		loop = loop.next.twin;
    		
    		
    	}while(loop!=A);
		
    	queue.add(A.e);
    	queue.add(C.e);
    	
    	return true;
    	}
    	}
    }


    
    private Set<Vertex> Ring (HalfEdge he) {
		HashSet<Vertex> set = new HashSet<>();
		HalfEdge current_he= he;
		
			
	 while (!set.contains(current_he.twin.head))
			
		{
			set.add(current_he.twin.head);
			current_he = current_he.next.twin;
			
			
		};
			

		return set;
	}


	public Boolean isTetrahedron() {
		return faces.size() <= 4;
	}
	/**
	 * An edge is  not collapsable if
	 *     1. It's a tetrahedron
	 *     2. The 1-rings of the edge vertices have more than 2 vertices in common
	 */    
	public Boolean IsCollapsible (HalfEdge he1) {

		if (isTetrahedron()) {
			return false;	
		}          //First check tetrahedron
		
		
		
		Collection<Vertex> set1 = Ring(he1);
		Collection<Vertex> set2 = Ring(he1.twin);
		
		int count = 0;
		for ( Vertex v : set1){
			if (set2.contains(v)) {
				count++;
			}
		}   	
		if (count > 2 ){
			return false;	
		}

		else {
			return true;
		}// Then check number of common vertices
	}
    
	
	
	private void VertexQ(HalfEdge HE) {
		Set<HalfEdge> set =  new HashSet<>();   
		
		HalfEdge he_to_add = HE;
		do {
			set.add(he_to_add);
			he_to_add = he_to_add.next.twin;
		} while (he_to_add != HE);
		Matrix4d accumulatedQ = new Matrix4d();
		for (HalfEdge sethe : set) {
			accumulatedQ.add(sethe.leftFace.K);
		} 
		HE.head.Q = accumulatedQ;
	}
	
	public double EdgeError(HalfEdge he1){
		Vector4d EdgevPosi = he1.e.v;
		
		
		Matrix4d tmp = new Matrix4d();
		tmp.setColumn(0, EdgevPosi);
		tmp.mul(he1.e.Q, tmp);
		
		Vector4d Rightresult = new Vector4d();
		tmp.getColumn(0, Rightresult); 
		
		double error = EdgevPosi.dot(Rightresult); //(v^t)Qv
		return error;
	}
	
	private void updatevertexQ(HalfEdge HE)
	{
		VertexQ(HE);
		
		VertexQ(HE.twin);		
	}
	
	private Point3d OptimalVPosition(HalfEdge HE) {
		
		
		Vector3d v21 = new Vector3d();
		v21.sub(HE.twin.head.p, HE.head.p);//HE's vector
		Point3d midv = new Point3d();
		midv.scaleAdd(0.5, v21, HE.head.p);//HE's mid v
		
		
		Matrix4d v1Q = HE.head.Q;
		Matrix4d v2Q = HE.twin.head.Q;		
		Matrix4d edgeQ = new Matrix4d();
		edgeQ.add(v1Q,v2Q);
		
		HE.e.Q=edgeQ;//Q without REG
		


		
		Matrix4d Qreg =  new Matrix4d();
		Qreg.setIdentity();	
		Vector4d tmpV = new Vector4d(midv.x,midv.y,midv.z,midv.x*midv.x+midv.y*midv.y+midv.z*midv.z);
		Qreg.setRow(3, tmpV);
		Qreg.setColumn(3, tmpV);

		Qreg.mul(weight,Qreg);
		Qreg.add(edgeQ);
		
		
		
		
		Matrix3d A=new Matrix3d();
		A.m00=Qreg.m00;
		A.m01=Qreg.m01;
		A.m02=Qreg.m02;
		A.m10=Qreg.m10;
		A.m11=Qreg.m11;
		A.m12=Qreg.m12;
		A.m20=Qreg.m20;
		A.m21=Qreg.m21;
		A.m22=Qreg.m22;
		
		Matrix3d B=new Matrix3d();
		B.setColumn(0, Qreg.m03,Qreg.m13,Qreg.m23);
		B.setColumn(1, 0,0,0);
		B.setColumn(2, 0,0,0);
		
		Vector3d vposition = new Vector3d();
		if(A.determinant()==00)
			return midv;
		else
		{
			Matrix3d Ainverse =  new Matrix3d();
			Ainverse.invert(A);
			Ainverse.mul(-1);
			Ainverse.mul(B);
			Ainverse.getColumn(0, vposition);
		}
		return new Point3d(vposition.x,vposition.y,vposition.z);
	}
    
    
    /**
	 * Need to know both verts before the collapse, but this information is actually 
	 * already stored within the excized portion of the half edge data structure.
	 * Thus, we only need to have a half edge (the collapsed half edge) to undo
	 */
	LinkedList<HalfEdge> undoList = new LinkedList<>();
	/**
	 * To redo an undone collapse, we must know which edge to collapse.  We should
	 * likewise reuse the Vertex that was created for the collapse.
	 */
	LinkedList<HalfEdge> redoListHalfEdge = new LinkedList<>();
	LinkedList<Vertex> redoListVertex = new LinkedList<>();

    void undoCollapse() {
    	if ( undoList.isEmpty() ) return; // ignore the request
   
    	
    	HalfEdge he = undoList.removeLast();
    	
    	Vertex hehead=he.head;
    	Vertex hetwinhead=he.twin.head;
    	HalfEdge A = he.next.twin;
    	HalfEdge B = he.next.next.twin;
    	HalfEdge C = he.twin.next.next.twin;
    	HalfEdge D = he.twin.next.twin;
    	
    	redoListVertex.add(A.head);
    	redoListHalfEdge.add(he);
    	
    	TwinsEdge(he.next,A);
    	TwinsEdge(he.next.next,B);
    	TwinsEdge(he.twin.next,D);
    	TwinsEdge(he.twin.next.next,C);
    	
    	queue.remove(A.e);
    	queue.remove(C.e);
    	HalfEdge loop = A;
    	do{    		
    		loop.head =hehead;   		
    		loop = loop.next.twin;
    				
    	}while(loop!=A);
    	
    	 loop = D;
    	do{    		
    		loop.head =hetwinhead;   		
    		loop = loop.next.twin;
    				
    	}while(loop!=D);
    	

    	
    	Face left=new Face(he);
    	faces.add(left);
    	Face twinleft=new Face(he.twin);
    	faces.add(twinleft);
    	
    	for(Face face:faces)
    	{
    		face.recomputeNormal();    		
    	}
    	
    	
    	queue.add(A.e);
    	queue.add(B.e);
    	queue.add(C.e);
    	queue.add(D.e);
    	queue.add(he.e);
    	
    	
    	
    	// TODO: Objective 6: undo the last collapse
    	// be sure to put the information on the redo list so you can redo the collapse too!
    }
    void TwinsEdge(HalfEdge he1, HalfEdge he2){
		he1.twin = he2;
		he2.twin = he1;
		he2.e = he1.e;
		he1.e.he = he1;
		he1.twin.e.he = he1;
	}
    void redoCollapse() {
    	if ( redoListHalfEdge.isEmpty() ) return; // ignore the request
    	
    	HalfEdge he = redoListHalfEdge.removeLast();
    	Vertex v = redoListVertex.removeLast();
    	
    	undoList.add( he );  // put this on the undo list so we can undo this collapse again
    	
    	

	HalfEdge A = he.next.twin;
	HalfEdge B = he.next.next.twin;
	HalfEdge C = he.twin.next.next.twin;
	HalfEdge D = he.twin.next.twin;


	queue.remove(he.e);
	queue.remove(A.e);
	queue.remove(B.e);
	queue.remove(C.e);
	queue.remove(D.e);
	
	
	A.twin = B;
	B.twin = A;
	C.twin = D;
	D.twin = C;

	
	
	faces.remove(he.leftFace);
	faces.remove(he.twin.leftFace);
	


	
	HalfEdge loop = A;
	do{    		
		loop.head =v;   		
		loop = loop.next.twin;
				
	}while(loop!=A);
	B.e=A.e;
	D.e=C.e;
	A.e.he=A;
	A.twin.e.he=A;
	C.e.he=C;
	C.twin.e.he=C;
	
	for(Face face:faces)
	{
		face.recomputeNormal();    		
	}


	
	queue.add(A.e);
	queue.add(C.e);
    	// TODO: Objective 7: undo the edge collapse!
    	
    }
    
      
    /**
     * Draws the half edge data structure by drawing each of its faces.
     * Per vertex normals are used to draw the smooth surface when available,
     * otherwise a face normal is computed. 
     * @param drawable
     */
    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();

        // we do not assume triangular faces here        
        Point3d p;
        Vector3d n;        
        for ( Face face : faces ) {
            HalfEdge he = face.he;
            gl.glBegin( GL2.GL_POLYGON );
            n = he.leftFace.n;
            gl.glNormal3d( n.x, n.y, n.z );
            HalfEdge e = he;
            do {
                p = e.head.p;
                gl.glVertex3d( p.x, p.y, p.z );
                e = e.next;
            } while ( e != he );
            gl.glEnd();
        }
    }

	public void setregularization(float floatValue) {
		
		weight=floatValue;
		// TODO Auto-generated method stub
		
	}
	public void drawpoint( HalfEdge currentHE, GLAutoDrawable drawable) {
		Vector4d vposition = new Vector4d(OptimalVPosition(currentHE));
		GL2 gl = drawable.getGL().getGL2();
		gl.glDisable(GL2.GL_LIGHTING);
		gl.glColor4f(0.0f,0.9f,0.9f,1);
		gl.glPointSize(10);
		gl.glBegin(GL2.GL_POINTS);
		gl.glVertex3d(vposition.x, vposition.y,vposition.z);
		gl.glEnd();
		gl.glEnable(GL2.GL_LIGHTING);
	}

	
	//Important! Always choose the HE that can be collapsed
	public HalfEdge setcollapseedge() {
		if(!isTetrahedron()) {
		HalfEdge result=queue.peek().he;
		while(!IsCollapsible(result))
		{
			queue.remove(queue.peek());
			result=queue.peek().he;
		}
		return result;}
		else return queue.peek().he;
		// TODO Auto-generated method stub
	
	}

}