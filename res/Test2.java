int[] values = new int[10];
for (int i = values.length - 1; i > 0; i--)
{
	if (i % 2 == 0)
	{
		values[i] = i*i;
	}
	else
	{
		values[i] = -i;
	}
}