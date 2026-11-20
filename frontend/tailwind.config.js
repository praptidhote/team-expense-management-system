/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        brand: {
          50: '#f0f5ff',
          100: '#e0ebff',
          200: '#c7d9fe',
          300: '#a4c0fc',
          400: '#7a9df8',
          500: '#537af2',
          600: '#385ce8',
          700: '#2b47cc',
          800: '#1e328a',
          900: '#1a2c6d',
          950: '#111b43',
        }
      }
    },
  },
  plugins: [],
}
