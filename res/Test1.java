int[] values = new int[4];
int[] values2 = values;
for (int i = 0; i < values.length; i++)
{
	values[i] = (i+1)*(i+1);
}
System.out.println("done");