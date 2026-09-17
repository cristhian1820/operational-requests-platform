import js from '@eslint/js';
import tseslint from '@typescript-eslint/eslint-plugin';
import parser from '@typescript-eslint/parser';
import hooks from 'eslint-plugin-react-hooks';
import globals from 'globals';
export default [
  { ignores: ['**/dist/**','**/storybook-static/**','**/coverage/**'] },
  js.configs.recommended,
  { files: ['**/*.{ts,tsx}'], languageOptions: { parser, parserOptions: { ecmaVersion: 2022, sourceType: 'module', ecmaFeatures: { jsx: true } }, globals: {...globals.browser,...globals.node} }, plugins: {'@typescript-eslint':tseslint,'react-hooks':hooks}, rules: {...tseslint.configs.recommended.rules,...hooks.configs.recommended.rules,'@typescript-eslint/no-explicit-any':'off','no-undef':'off','react-hooks/set-state-in-effect':'off','react-hooks/exhaustive-deps':'off'} }
];
